package com.platform.admin.modules.backup.support;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.config.BackupProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Component
public class BackupExecutor {

    private static final Logger log = LoggerFactory.getLogger(BackupExecutor.class);
    private static final Pattern JDBC_URL = Pattern.compile(
            "jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?]+)"
    );

    private final BackupProperties backupProperties;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public BackupExecutor(
            BackupProperties backupProperties,
            @Value("${spring.datasource.url:}") String jdbcUrl,
            @Value("${spring.datasource.username:}") String username,
            @Value("${spring.datasource.password:}") String password
    ) {
        this.backupProperties = backupProperties;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * 执行 mysqldump 并 gzip 压缩到目标文件。
     *
     * @param targetFile 输出 .sql.gz 文件路径
     */
    public void dumpDatabase(Path targetFile) {
        DatabaseConnection conn = parseConnection();
        try {
            Files.createDirectories(targetFile.getParent());
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建备份目录失败");
        }

        List<String> command = buildDumpCommand(conn);
        log.info("event=backup_dump_start file={} command={}", targetFile, maskCommandForLog(command));

        ProcessBuilder dumpBuilder = new ProcessBuilder(command);
        applyPasswordEnv(dumpBuilder);

        try {
            Process dumpProcess = dumpBuilder.start();
            byte[] processOutput = readProcessOutput(dumpProcess);
            int exitCode = dumpProcess.waitFor();
            if (exitCode != 0) {
                deleteQuietly(targetFile);
                String detail = truncateOutput(processOutput);
                log.error("event=backup_dump_failed file={} exitCode={} output={}", targetFile, exitCode, detail);
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, buildDumpFailureMessage(exitCode, detail));
            }
            try (OutputStream fileOut = Files.newOutputStream(targetFile);
                 InputStream sqlIn = new java.io.ByteArrayInputStream(processOutput)) {
                gzipStream(sqlIn, fileOut);
            }
            log.info("event=backup_dump_success file={} bytes={}", targetFile, processOutput.length);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            deleteQuietly(targetFile);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "备份执行被中断");
        } catch (IOException ex) {
            deleteQuietly(targetFile);
            log.error("event=backup_dump_failed file={}", targetFile, ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, buildIoFailureMessage(ex));
        }
    }

    /**
     * 向已生成的 .sql.gz 末尾追加 UPDATE，修正 dump 时固化的 IN_PROGRESS 状态。
     */
    public void appendBackupRecordFixup(Path gzipFile, String objectId, long fileSize, String filePath) {
        String sql = String.format(
                "UPDATE backup_record SET status='SUCCESS', file_size=%d, file_path='%s' WHERE object_id='%s';%n",
                fileSize,
                escapeSqlLiteral(filePath),
                escapeSqlLiteral(objectId)
        );
        try {
            appendSqlToGzip(gzipFile, sql);
        } catch (IOException ex) {
            log.warn("event=backup_fixup_append_failed file={} objectId={}", gzipFile, objectId, ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "备份元数据修正失败");
        }
    }

    private void appendSqlToGzip(Path gzipFile, String sql) throws IOException {
        Path tempDir = Files.createTempDirectory("backup-fixup-");
        Path plainSql = tempDir.resolve("dump.sql");
        Path patchedGzip = tempDir.resolve("dump.sql.gz");
        try (GZIPInputStream gzipIn = new GZIPInputStream(Files.newInputStream(gzipFile));
             OutputStream plainOut = Files.newOutputStream(plainSql)) {
            gzipIn.transferTo(plainOut);
        }
        Files.writeString(plainSql, sql, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        try (InputStream plainIn = Files.newInputStream(plainSql);
             GZIPOutputStream gzipOut = new GZIPOutputStream(Files.newOutputStream(patchedGzip))) {
            plainIn.transferTo(gzipOut);
        }
        Files.move(patchedGzip, gzipFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private String escapeSqlLiteral(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
    }

    /**
     * 解压 .sql.gz 并通过 mysql 客户端导入。
     *
     * @param backupFile 备份文件路径
     */
    public void restoreDatabase(Path backupFile) {
        if (!Files.exists(backupFile)) {
            throw new BusinessException(ErrorCode.BACKUP_FILE_NOT_FOUND, "备份文件不存在");
        }
        DatabaseConnection conn = parseConnection();
        List<String> command = buildMysqlCommand(conn);
        log.info("event=backup_restore_start file={} command={}", backupFile, maskCommandForLog(command));

        ProcessBuilder builder = new ProcessBuilder(command);
        applyPasswordEnv(builder);
        if (usesDockerExec()) {
            builder.redirectErrorStream(true);
        }

        try {
            Process process = builder.start();
            try (GZIPInputStream gzipIn = new GZIPInputStream(Files.newInputStream(backupFile));
                 OutputStream mysqlIn = process.getOutputStream()) {
                gzipIn.transferTo(mysqlIn);
            }
            byte[] processOutput = readProcessOutput(process);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String detail = truncateOutput(processOutput);
                log.error("event=backup_restore_failed file={} exitCode={} output={}", backupFile, exitCode, detail);
                throw new BusinessException(ErrorCode.RESTORE_FAILED, buildRestoreFailureMessage(exitCode, detail));
            }
            log.info("event=backup_restore_success file={}", backupFile);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.RESTORE_FAILED, "恢复执行被中断");
        } catch (IOException ex) {
            log.error("event=backup_restore_failed file={}", backupFile, ex);
            throw new BusinessException(ErrorCode.RESTORE_FAILED, buildIoFailureMessage(ex));
        }
    }

    private List<String> buildDumpCommand(DatabaseConnection conn) {
        List<String> dumpArgs = new ArrayList<>();
        dumpArgs.add("-u");
        dumpArgs.add(username);
        dumpArgs.add("--single-transaction");
        dumpArgs.add("--routines");
        dumpArgs.add("--triggers");
        dumpArgs.add("--column-statistics=0");
        dumpArgs.add("--set-gtid-purged=OFF");
        if (!usesDockerExec()) {
            dumpArgs.add("-h");
            dumpArgs.add(conn.host());
            dumpArgs.add("-P");
            dumpArgs.add(String.valueOf(conn.port()));
        }
        dumpArgs.add(conn.database());
        return wrapWithRuntime(dumpArgs, backupProperties.getMysqldumpPath());
    }

    private List<String> buildMysqlCommand(DatabaseConnection conn) {
        List<String> mysqlArgs = new ArrayList<>();
        mysqlArgs.add("-u");
        mysqlArgs.add(username);
        if (!usesDockerExec()) {
            mysqlArgs.add("-h");
            mysqlArgs.add(conn.host());
            mysqlArgs.add("-P");
            mysqlArgs.add(String.valueOf(conn.port()));
        }
        mysqlArgs.add(conn.database());
        return wrapWithRuntime(mysqlArgs, backupProperties.getMysqlPath());
    }

    /**
     * 开发环境可通过 docker exec 调用容器内客户端，避免宿主机 PATH/版本不一致。
     */
    private List<String> wrapWithRuntime(List<String> clientArgs, String clientBinary) {
        if (!usesDockerExec()) {
            List<String> command = new ArrayList<>();
            command.add(clientBinary);
            command.addAll(clientArgs);
            return command;
        }
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("exec");
        if (StringUtils.hasText(password)) {
            command.add("-e");
            command.add("MYSQL_PWD=" + password);
        }
        if ("mysql".equals(clientBinary) || clientBinary.endsWith("/mysql")) {
            command.add("-i");
        }
        command.add(backupProperties.getDockerContainer());
        command.add(clientBinary);
        command.addAll(clientArgs);
        return command;
    }

    private void applyPasswordEnv(ProcessBuilder builder) {
        if (StringUtils.hasText(password) && !usesDockerExec()) {
            builder.environment().put("MYSQL_PWD", password);
        }
        builder.redirectErrorStream(true);
    }

    private boolean usesDockerExec() {
        return StringUtils.hasText(backupProperties.getDockerContainer());
    }

    private byte[] readProcessOutput(Process process) throws IOException {
        try (InputStream stream = process.getInputStream();
             ByteArrayOutputStream capture = new ByteArrayOutputStream()) {
            stream.transferTo(capture);
            return capture.toByteArray();
        }
    }

    private String buildDumpFailureMessage(int exitCode, String detail) {
        if (!StringUtils.hasText(detail)) {
            return "备份执行失败（exitCode=" + exitCode + "）";
        }
        return "备份执行失败（exitCode=" + exitCode + "）: " + detail;
    }

    private String buildRestoreFailureMessage(int exitCode, String detail) {
        if (!StringUtils.hasText(detail)) {
            return "恢复失败（exitCode=" + exitCode + "）";
        }
        return "恢复失败（exitCode=" + exitCode + "）: " + detail;
    }

    private String buildIoFailureMessage(IOException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        if (message.contains("No such file or directory") || message.contains("error=2")) {
            String tool = usesDockerExec() ? "docker" : backupProperties.getMysqldumpPath();
            return "找不到备份客户端命令: " + tool + "，请检查 app.backup 配置或安装 MySQL 客户端";
        }
        return "备份进程启动失败: " + message;
    }

    private String maskCommandForLog(List<String> command) {
        return String.join(" ", command).replaceAll("MYSQL_PWD=[^\\s]+", "MYSQL_PWD=***");
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            log.warn("delete backup file failed path={}", path, ex);
        }
    }

    private String truncateOutput(byte[] output) {
        if (output == null || output.length == 0) {
            return "";
        }
        String text = new String(output, StandardCharsets.UTF_8).trim();
        return text.length() > 500 ? text.substring(0, 500) + "..." : text;
    }

    private void gzipStream(InputStream input, OutputStream output) throws IOException {
        java.util.zip.GZIPOutputStream gzipOut = new java.util.zip.GZIPOutputStream(output);
        input.transferTo(gzipOut);
        gzipOut.finish();
    }

    private DatabaseConnection parseConnection() {
        Matcher matcher = JDBC_URL.matcher(jdbcUrl);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "无法解析数据库连接配置");
        }
        String host = matcher.group(1);
        int port = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 3306;
        String database = matcher.group(3);
        return new DatabaseConnection(host, port, database);
    }

    private record DatabaseConnection(String host, int port, String database) {
    }
}

package com.platform.admin.modules.backup.support;

import com.platform.admin.config.BackupProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class BackupStorage {

    private static final Logger log = LoggerFactory.getLogger(BackupStorage.class);
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BackupProperties properties;
    private Path resolvedRoot;

    public BackupStorage(BackupProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() throws IOException {
        this.resolvedRoot = resolveRoot();
        Files.createDirectories(resolvedRoot);
        log.info("backup storage root={}", resolvedRoot);
    }

    public Path getRoot() {
        return resolvedRoot;
    }

  /**
     * 生成备份文件名：{objectId}_{backupType}_{yyyyMMddHHmmss}.sql.gz
     */
    public String buildFileName(String objectId, String backupType, LocalDateTime time) {
        return objectId + "_" + backupType + "_" + FILE_TIME.format(time) + ".sql.gz";
    }

    public Path resolveFilePath(String fileName) {
        return resolvedRoot.resolve(fileName).normalize();
    }

    public Path resolveStoredFile(String storedPath) {
        if (!StringUtils.hasText(storedPath)) {
            return null;
        }
        Path path = Paths.get(storedPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return resolvedRoot.resolve(storedPath).normalize();
    }

    private Path resolveRoot() throws IOException {
        if (StringUtils.hasText(properties.getStorageDirectory())) {
            return Paths.get(properties.getStorageDirectory()).toAbsolutePath().normalize();
        }
        ClassPathResource resource = new ClassPathResource("backups");
        try {
            return resource.getFile().toPath().toAbsolutePath().normalize();
        } catch (IOException ex) {
            Path fallback = Paths.get(System.getProperty("java.io.tmpdir"), "admin-server-backups")
                    .toAbsolutePath()
                    .normalize();
            log.warn("classpath backups is not writable; using {}", fallback);
            return fallback;
        }
    }
}

package com.platform.admin.modules.log.support;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理导出 CSV 临时文件与 fileId 映射，支持过期校验。
 */
@Component
public class LogExportFileRegistry {

    private static final DateTimeFormatter FILE_ID_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Map<String, ExportFileEntry> files = new ConcurrentHashMap<>();

    /**
     * 注册导出文件并返回 fileId。
     *
     * @param path       文件路径
     * @param expireTime 过期时间
     * @param type       导出类型（OPERATION/SYSTEM/SECURITY）
     * @return fileId
     */
    public String register(Path path, LocalDateTime expireTime, String type) {
        String safeType = type == null ? "unknown" : type.toLowerCase();
        String fileId = "log_" + safeType + "_" + FILE_ID_TIME.format(LocalDateTime.now()) + "_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        files.put(fileId, new ExportFileEntry(path, expireTime));
        return fileId;
    }

    /**
     * 根据 fileId 解析导出文件路径。
     *
     * @param fileId 文件标识
     * @return 文件路径
     */
    public Path resolve(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "fileId不能为空");
        }
        ExportFileEntry entry = files.get(fileId);
        if (entry == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        if (entry.expireTime().isBefore(LocalDateTime.now())) {
            files.remove(fileId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return entry.path();
    }

    public record ExportFileEntry(Path path, LocalDateTime expireTime) {
    }
}

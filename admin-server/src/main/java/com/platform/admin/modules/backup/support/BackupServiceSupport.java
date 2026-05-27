package com.platform.admin.modules.backup.support;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.config.BackupProperties;
import com.platform.admin.modules.backup.vo.StorageInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class BackupServiceSupport {

    private static final Logger log = LoggerFactory.getLogger(BackupServiceSupport.class);

    private final BackupProperties backupProperties;
    private final BackupStorage backupStorage;

    public BackupServiceSupport(BackupProperties backupProperties, BackupStorage backupStorage) {
        this.backupProperties = backupProperties;
        this.backupStorage = backupStorage;
    }

    public void ensureAvailableSpace() {
        long usedBytes = calculateUsedBytes();
        long usedSpaceMB = usedBytes / (1024 * 1024);
        long available = backupProperties.getTotalSpaceMb() - usedSpaceMB;
        if (available <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "备份存储空间不足");
        }
    }

    public long calculateUsedBytes() {
        Path root = backupStorage.getRoot();
        if (!Files.exists(root)) {
            return 0L;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile).mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException ex) {
                    return 0L;
                }
            }).sum();
        } catch (IOException ex) {
            log.warn("calculate backup used space failed root={}", root, ex);
            return 0L;
        }
    }

    public StorageInfoVO buildStorageInfo(long backupCount) {
        long usedSpaceMB = calculateUsedBytes() / (1024 * 1024);
        long totalSpaceMB = backupProperties.getTotalSpaceMb();
        return StorageInfoVO.builder()
                .totalSpaceMB(totalSpaceMB)
                .usedSpaceMB(usedSpaceMB)
                .backupCount(backupCount)
                .availableSpaceMB(Math.max(0, totalSpaceMB - usedSpaceMB))
                .build();
    }
}

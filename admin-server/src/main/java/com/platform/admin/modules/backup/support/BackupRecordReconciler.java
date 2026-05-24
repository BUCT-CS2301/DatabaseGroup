package com.platform.admin.modules.backup.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.platform.admin.config.BackupProperties;
import com.platform.admin.modules.backup.entity.BackupRecordEntity;
import com.platform.admin.modules.backup.enums.BackupStatus;
import com.platform.admin.modules.backup.mapper.BackupRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 清理卡住的 IN_PROGRESS 备份记录，避免互斥检查永久返回 409。
 */
@Component
public class BackupRecordReconciler {

    private static final Logger log = LoggerFactory.getLogger(BackupRecordReconciler.class);

    private final BackupRecordMapper backupRecordMapper;
    private final BackupProperties backupProperties;

    public BackupRecordReconciler(BackupRecordMapper backupRecordMapper, BackupProperties backupProperties) {
        this.backupRecordMapper = backupRecordMapper;
        this.backupProperties = backupProperties;
    }

    /**
     * 恢复完成后校正 backup_record：备份快照里可能固化 IN_PROGRESS（mysqldump 早于 SUCCESS 更新）。
     * 若磁盘上备份文件存在且非空，则修正为 SUCCESS。
     */
    public int reconcileAfterRestore(BackupStorage backupStorage) {
        List<BackupRecordEntity> inProgressRecords = backupRecordMapper.selectList(
                new LambdaQueryWrapper<BackupRecordEntity>()
                        .eq(BackupRecordEntity::getStatus, BackupStatus.IN_PROGRESS)
        );
        int fixed = 0;
        for (BackupRecordEntity record : inProgressRecords) {
            if (reconcileSingleRecord(record, backupStorage)) {
                fixed++;
            }
        }
        if (fixed > 0) {
            log.info("event=backup_reconcile_after_restore fixed={}", fixed);
        }
        return fixed;
    }

    /**
     * 根据磁盘文件校正单条 IN_PROGRESS 记录。
     */
    public boolean reconcileSingleRecord(BackupRecordEntity record, BackupStorage backupStorage) {
        Path file = backupStorage.resolveStoredFile(record.getFilePath());
        if (file == null || !Files.exists(file)) {
            record.setStatus(BackupStatus.FAILED);
            backupRecordMapper.updateById(record);
            log.warn("event=backup_reconcile_mark_failed objectId={} reason=file_missing", record.getObjectId());
            return true;
        }
        try {
            long size = Files.size(file);
            if (size <= 0) {
                record.setStatus(BackupStatus.FAILED);
                backupRecordMapper.updateById(record);
                return true;
            }
            record.setStatus(BackupStatus.SUCCESS);
            record.setFileSize(size);
            record.setFilePath(file.toString());
            backupRecordMapper.updateById(record);
            return true;
        } catch (IOException ex) {
            log.warn("event=backup_reconcile_skip objectId={}", record.getObjectId(), ex);
            return false;
        }
    }

    /**
     * 服务启动后，将遗留的 IN_PROGRESS 记录标记为 FAILED（进程已不存在）。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reconcileOnStartup() {
        int updated = markInProgressBefore(BackupCronUtil.now());
        if (updated > 0) {
            log.warn("event=backup_reconcile_startup markedFailed={}", updated);
        }
    }

    /**
     * 新备份前回收超时未完成的 IN_PROGRESS 记录。
     */
    public void reconcileStaleRecords() {
        LocalDateTime cutoff = BackupCronUtil.now()
                .minusMinutes(backupProperties.getInProgressTimeoutMinutes());
        int updated = markInProgressBefore(cutoff);
        if (updated > 0) {
            log.warn("event=backup_reconcile_stale markedFailed={} timeoutMinutes={}",
                    updated, backupProperties.getInProgressTimeoutMinutes());
        }
    }

    /**
     * 是否存在仍在有效执行窗口内的 IN_PROGRESS 备份。
     */
    public boolean hasActiveInProgressBackup() {
        reconcileStaleRecords();
        LocalDateTime cutoff = BackupCronUtil.now()
                .minusMinutes(backupProperties.getInProgressTimeoutMinutes());
        Long count = backupRecordMapper.selectCount(
                new LambdaQueryWrapper<BackupRecordEntity>()
                        .eq(BackupRecordEntity::getStatus, BackupStatus.IN_PROGRESS)
                        .ge(BackupRecordEntity::getCreateTime, cutoff)
        );
        return count != null && count > 0;
    }

    private int markInProgressBefore(LocalDateTime cutoff) {
        LambdaUpdateWrapper<BackupRecordEntity> update = new LambdaUpdateWrapper<>();
        update.eq(BackupRecordEntity::getStatus, BackupStatus.IN_PROGRESS)
                .lt(BackupRecordEntity::getCreateTime, cutoff)
                .set(BackupRecordEntity::getStatus, BackupStatus.FAILED);
        return backupRecordMapper.update(null, update);
    }
}

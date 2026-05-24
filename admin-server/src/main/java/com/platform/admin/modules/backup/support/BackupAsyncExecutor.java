package com.platform.admin.modules.backup.support;

import com.platform.admin.modules.backup.entity.BackupRecordEntity;
import com.platform.admin.modules.backup.enums.BackupStatus;
import com.platform.admin.modules.backup.enums.RestoreTaskStatus;
import com.platform.admin.modules.backup.mapper.BackupRecordMapper;
import com.platform.admin.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class BackupAsyncExecutor {

    private static final Logger log = LoggerFactory.getLogger(BackupAsyncExecutor.class);

    private final BackupRecordMapper backupRecordMapper;
    private final BackupExecutor backupExecutor;
    private final RestoreTaskStore restoreTaskStore;
    private final BackupServiceSupport backupServiceSupport;
    private final BackupStorage backupStorage;
    private final BackupRecordReconciler backupRecordReconciler;

    public BackupAsyncExecutor(
            BackupRecordMapper backupRecordMapper,
            BackupExecutor backupExecutor,
            RestoreTaskStore restoreTaskStore,
            BackupServiceSupport backupServiceSupport,
            BackupStorage backupStorage,
            BackupRecordReconciler backupRecordReconciler
    ) {
        this.backupRecordMapper = backupRecordMapper;
        this.backupExecutor = backupExecutor;
        this.restoreTaskStore = restoreTaskStore;
        this.backupServiceSupport = backupServiceSupport;
        this.backupStorage = backupStorage;
        this.backupRecordReconciler = backupRecordReconciler;
    }

    @Async
    public void executeBackupAsync(String objectId, Path targetFile) {
        BackupRecordEntity record = backupRecordMapper.selectById(objectId);
        if (record == null) {
            log.error("event=backup_async_skip reason=record_not_found objectId={}", objectId);
            return;
        }
        try {
            backupServiceSupport.ensureAvailableSpace();
            backupExecutor.dumpDatabase(targetFile);
            long sizeAfterDump = Files.size(targetFile);
            // dump 时记录仍为 IN_PROGRESS，需在 SQL 末尾追加 UPDATE，避免 restore 后状态回退
            backupExecutor.appendBackupRecordFixup(targetFile, objectId, sizeAfterDump, targetFile.toString());
            long finalSize = Files.size(targetFile);
            record.setFileSize(finalSize);
            record.setFilePath(targetFile.toString());
            record.setStatus(BackupStatus.SUCCESS);
            backupRecordMapper.updateById(record);
            log.info("event=backup_async_success objectId={} size={}", objectId, finalSize);
        } catch (BusinessException ex) {
            markBackupFailed(record);
            log.error("event=backup_async_failed objectId={} reason={}", objectId, ex.getMessage());
        } catch (Exception ex) {
            markBackupFailed(record);
            log.error("event=backup_async_failed objectId={}", objectId, ex);
        }
    }

    @Async
    public void executeRestoreAsync(String restoreTaskId, Path backupFile) {
        try {
            backupExecutor.restoreDatabase(backupFile);
            backupRecordReconciler.reconcileAfterRestore(backupStorage);
            restoreTaskStore.updateStatus(restoreTaskId, RestoreTaskStatus.SUCCESS);
            log.info("event=backup_restore_async_success restoreTaskId={}", restoreTaskId);
        } catch (BusinessException ex) {
            restoreTaskStore.updateStatus(restoreTaskId, RestoreTaskStatus.FAILED);
            log.error("event=backup_restore_async_failed restoreTaskId={} reason={}", restoreTaskId, ex.getMessage());
        } catch (Exception ex) {
            restoreTaskStore.updateStatus(restoreTaskId, RestoreTaskStatus.FAILED);
            log.error("event=backup_restore_async_failed restoreTaskId={}", restoreTaskId, ex);
        } finally {
            restoreTaskStore.clearRestoreActive();
        }
    }

    private void markBackupFailed(BackupRecordEntity record) {
        record.setStatus(BackupStatus.FAILED);
        backupRecordMapper.updateById(record);
    }
}

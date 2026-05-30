package com.platform.admin.modules.backup.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.modules.backup.entity.BackupScheduleEntity;
import com.platform.admin.modules.backup.mapper.BackupScheduleMapper;
import com.platform.admin.modules.backup.service.BackupServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BackupScheduleRunner {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduleRunner.class);

    private final BackupScheduleMapper backupScheduleMapper;
    private final RestoreTaskStore restoreTaskStore;
    private final BackupRecordReconciler backupRecordReconciler;
    private final BackupServiceImpl backupService;

    public BackupScheduleRunner(
            BackupScheduleMapper backupScheduleMapper,
            RestoreTaskStore restoreTaskStore,
            BackupRecordReconciler backupRecordReconciler,
            BackupServiceImpl backupService
    ) {
        this.backupScheduleMapper = backupScheduleMapper;
        this.restoreTaskStore = restoreTaskStore;
        this.backupRecordReconciler = backupRecordReconciler;
        this.backupService = backupService;
    }

    /**
     * 每分钟扫描到期的定时备份任务。
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runDueSchedules() {
        LocalDateTime now = BackupCronUtil.now();
        LambdaQueryWrapper<BackupScheduleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BackupScheduleEntity::getEnabled, true)
                .le(BackupScheduleEntity::getNextExecutionTime, now);
        List<BackupScheduleEntity> dueSchedules = backupScheduleMapper.selectList(wrapper);
        for (BackupScheduleEntity schedule : dueSchedules) {
            try {
                if (restoreTaskStore.hasProcessingTask() || backupRecordReconciler.hasActiveInProgressBackup()) {
                    log.warn("defer scheduled backup scheduleId={}", schedule.getObjectId());
                    continue;
                }
                backupService.triggerScheduledBackup(schedule);
            } catch (Exception ex) {
                log.error("scheduled backup failed scheduleId={}", schedule.getObjectId(), ex);
            }
        }
    }
}

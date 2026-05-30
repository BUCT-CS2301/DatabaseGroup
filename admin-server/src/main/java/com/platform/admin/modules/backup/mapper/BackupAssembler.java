package com.platform.admin.modules.backup.mapper;

import com.platform.admin.modules.backup.entity.BackupRecordEntity;
import com.platform.admin.modules.backup.entity.BackupScheduleEntity;
import com.platform.admin.modules.backup.vo.BackupRecordDetailVO;
import com.platform.admin.modules.backup.vo.BackupRecordVO;
import com.platform.admin.modules.backup.vo.BackupScheduleVO;

public final class BackupAssembler {

    private BackupAssembler() {
    }

    public static BackupRecordVO toRecordVO(BackupRecordEntity entity, String operatorName) {
        return BackupRecordVO.builder()
                .objectId(entity.getObjectId())
                .backupType(entity.getBackupType())
                .fileSize(entity.getFileSize())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .operatorId(entity.getOperatorId())
                .operatorName(operatorName)
                .createTime(entity.getCreateTime())
                .build();
    }

    public static BackupRecordDetailVO toRecordDetailVO(
            BackupRecordEntity entity,
            String operatorName,
            String downloadUrl
    ) {
        return BackupRecordDetailVO.builder()
                .objectId(entity.getObjectId())
                .backupType(entity.getBackupType())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .operatorId(entity.getOperatorId())
                .operatorName(operatorName)
                .downloadUrl(downloadUrl)
                .createTime(entity.getCreateTime())
                .build();
    }

    public static BackupScheduleVO toScheduleVO(BackupScheduleEntity entity) {
        return BackupScheduleVO.builder()
                .objectId(entity.getObjectId())
                .cronExpression(entity.getCronExpression())
                .backupType(entity.getBackupType())
                .enabled(entity.getEnabled())
                .description(entity.getDescription())
                .lastExecutionTime(entity.getLastExecutionTime())
                .nextExecutionTime(entity.getNextExecutionTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}

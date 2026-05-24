package com.platform.admin.modules.backup.service;

import com.platform.admin.common.PageResult;
import com.platform.admin.modules.backup.dto.CreateScheduleRequest;
import com.platform.admin.modules.backup.dto.ManualBackupRequest;
import com.platform.admin.modules.backup.dto.UpdateScheduleRequest;
import com.platform.admin.modules.backup.enums.BackupStatus;
import com.platform.admin.modules.backup.enums.BackupType;
import com.platform.admin.modules.backup.vo.BackupRecordDetailVO;
import com.platform.admin.modules.backup.vo.BackupRecordVO;
import com.platform.admin.modules.backup.vo.BackupScheduleVO;
import com.platform.admin.modules.backup.vo.DeleteBackupVO;
import com.platform.admin.modules.backup.vo.DeleteScheduleVO;
import com.platform.admin.modules.backup.vo.ManualBackupVO;
import com.platform.admin.modules.backup.vo.RestoreTaskVO;
import com.platform.admin.modules.backup.vo.StorageInfoVO;

import java.nio.file.Path;
import java.util.List;

public interface BackupService {

    ManualBackupVO triggerManualBackup(ManualBackupRequest request);

    List<BackupScheduleVO> listSchedules();

    BackupScheduleVO createSchedule(CreateScheduleRequest request);

    BackupScheduleVO updateSchedule(String objectId, UpdateScheduleRequest request);

    DeleteScheduleVO deleteSchedule(String objectId);

    PageResult<BackupRecordVO> pageRecords(
            long page,
            long pageSize,
            BackupStatus status,
            BackupType backupType
    );

    BackupRecordDetailVO getRecordDetail(String objectId, String baseUrl);

    Path resolveDownloadFile(String objectId);

    DeleteBackupVO deleteRecord(String objectId);

    RestoreTaskVO restoreFromBackup(String objectId);

    RestoreTaskVO getRestoreTask(String restoreTaskId);

    StorageInfoVO getStorageInfo();
}

package com.platform.admin.modules.backup.vo;

import com.platform.admin.modules.backup.enums.BackupStatus;
import com.platform.admin.modules.backup.enums.BackupType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ManualBackupVO {

    private String objectId;
    private BackupType backupType;
    private BackupStatus status;
    private String description;
    private LocalDateTime createTime;
}

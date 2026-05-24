package com.platform.admin.modules.backup.vo;

import com.platform.admin.modules.backup.enums.BackupStatus;
import com.platform.admin.modules.backup.enums.BackupType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BackupRecordDetailVO {

    private String objectId;
    private BackupType backupType;
    private String filePath;
    private Long fileSize;
    private BackupStatus status;
    private String description;
    private String operatorId;
    private String operatorName;
    private String downloadUrl;
    private LocalDateTime createTime;
}

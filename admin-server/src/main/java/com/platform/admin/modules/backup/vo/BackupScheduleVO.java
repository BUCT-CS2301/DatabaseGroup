package com.platform.admin.modules.backup.vo;

import com.platform.admin.modules.backup.enums.BackupType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BackupScheduleVO {

    private String objectId;
    private String cronExpression;
    private BackupType backupType;
    private Boolean enabled;
    private String description;
    private LocalDateTime lastExecutionTime;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

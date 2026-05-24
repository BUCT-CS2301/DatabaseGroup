package com.platform.admin.modules.backup.dto;

import com.platform.admin.modules.backup.enums.BackupType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateScheduleRequest {

    @Size(max = 50, message = "cronExpression长度不能超过50")
    private String cronExpression;

    private BackupType backupType;

    private Boolean enabled;

    @Size(max = 500, message = "description长度不能超过500")
    private String description;
}

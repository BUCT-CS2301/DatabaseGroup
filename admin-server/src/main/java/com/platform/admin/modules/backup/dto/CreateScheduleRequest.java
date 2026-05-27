package com.platform.admin.modules.backup.dto;

import com.platform.admin.modules.backup.enums.BackupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateScheduleRequest {

    @NotBlank(message = "cronExpression不能为空")
    @Size(max = 50, message = "cronExpression长度不能超过50")
    private String cronExpression;

    @NotNull(message = "backupType不能为空")
    private BackupType backupType;

    private Boolean enabled;

    @Size(max = 500, message = "description长度不能超过500")
    private String description;
}

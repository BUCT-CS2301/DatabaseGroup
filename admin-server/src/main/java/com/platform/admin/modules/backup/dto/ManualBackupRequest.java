package com.platform.admin.modules.backup.dto;

import com.platform.admin.modules.backup.enums.BackupType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ManualBackupRequest {

    @NotNull(message = "backupType不能为空")
    private BackupType backupType;

    @Size(max = 500, message = "description长度不能超过500")
    private String description;
}

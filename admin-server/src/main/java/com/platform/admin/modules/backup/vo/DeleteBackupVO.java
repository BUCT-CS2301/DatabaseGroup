package com.platform.admin.modules.backup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteBackupVO {
    private String objectId;
    private boolean deleted;
}

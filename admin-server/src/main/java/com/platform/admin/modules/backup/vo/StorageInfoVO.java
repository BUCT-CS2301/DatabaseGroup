package com.platform.admin.modules.backup.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageInfoVO {

    private long totalSpaceMB;
    private long usedSpaceMB;
    private long backupCount;
    private long availableSpaceMB;
}

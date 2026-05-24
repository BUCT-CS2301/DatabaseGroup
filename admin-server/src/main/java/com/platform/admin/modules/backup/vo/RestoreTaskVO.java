package com.platform.admin.modules.backup.vo;

import com.platform.admin.modules.backup.enums.RestoreTaskStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestoreTaskVO {

    private String restoreTaskId;
    private String backupRecordId;
    private RestoreTaskStatus status;
}

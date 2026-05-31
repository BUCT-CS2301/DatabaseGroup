package com.platform.admin.modules.log.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogVO {
    private String objectId;
    private LocalDateTime createTime;
    private String level;
    private String serviceName;
    private String messageSummary;
}

package com.platform.admin.modules.log.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityLogVO {
    private String objectId;
    private String eventType;
    private String eventResult;
    private String userIdentity;
    private String ipAddress;
    private LocalDateTime createTime;
}

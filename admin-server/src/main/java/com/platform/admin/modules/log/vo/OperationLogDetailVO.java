package com.platform.admin.modules.log.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDetailVO {
    private String objectId;
    private String userId;
    private String operator;
    private String module;
    private String action;
    private String result;
    private String ipAddress;
    private LocalDateTime operationTime;
    private Map<String, Object> requestParams;
    private Map<String, Object> responseResult;
}

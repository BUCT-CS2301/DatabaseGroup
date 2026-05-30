package com.platform.admin.modules.log.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogVO {
    private String objectId;
    private String userId;
    private String operator;
    private String module;
    private String action;
    private String result;
    private LocalDateTime operationTime;
}

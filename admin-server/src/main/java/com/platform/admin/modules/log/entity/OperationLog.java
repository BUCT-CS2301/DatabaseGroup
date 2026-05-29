package com.platform.admin.modules.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.ASSIGN_UUID)
    private String objectId;
    private String operationType;
    private String targetType;
    private String targetId;
    private String operatorId;
    private String operatorName;
    private String detail;
    private String result;
    private LocalDateTime operationTime;
}
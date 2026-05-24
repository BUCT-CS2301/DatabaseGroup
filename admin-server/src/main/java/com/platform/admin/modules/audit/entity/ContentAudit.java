package com.platform.admin.modules.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_queue")
public class ContentAudit {
    @TableId(type = IdType.ASSIGN_UUID)
    private String objectId;
    private String contentType;
    private String contentText;
    private String contentUrl;
    private String authorId;
    private String autoAuditResult;
    private String autoAuditDetail;
    private String status;
    private LocalDateTime submitTime;
    private String auditUserId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private String rejectReason;
}
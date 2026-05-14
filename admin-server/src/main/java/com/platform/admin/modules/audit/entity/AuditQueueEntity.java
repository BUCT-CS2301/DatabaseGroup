package com.platform.admin.modules.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("audit_queue")
public class AuditQueueEntity {

    @TableId(value = "object_id", type = IdType.ASSIGN_UUID)
    private String objectId;

    @TableField("content_type")
    private String contentType;

    @TableField("content_text")
    private String contentText;

    @TableField("content_url")
    private String contentUrl;

    @TableField("author_id")
    private String authorId;

    @TableField("auto_audit_result")
    private String autoAuditResult;

    @TableField("auto_audit_detail")
    private String autoAuditDetail;

    @TableField("status")
    private String status;

    @TableField(value = "submit_time", fill = FieldFill.INSERT)
    private LocalDateTime submitTime;

    @TableField("audit_user_id")
    private String auditUserId;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("audit_remark")
    private String auditRemark;

    @TableField("reject_reason")
    private String rejectReason;

    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAutoAuditResult() { return autoAuditResult; }
    public void setAutoAuditResult(String autoAuditResult) { this.autoAuditResult = autoAuditResult; }
    public String getAutoAuditDetail() { return autoAuditDetail; }
    public void setAutoAuditDetail(String autoAuditDetail) { this.autoAuditDetail = autoAuditDetail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public String getAuditUserId() { return auditUserId; }
    public void setAuditUserId(String auditUserId) { this.auditUserId = auditUserId; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
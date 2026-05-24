package com.platform.admin.modules.audit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.admin.modules.audit.entity.ContentAudit;

public interface ContentAuditService extends IService<ContentAudit> {
    ContentAudit submitForAudit(String content, String contentType, String authorId);
    ContentAudit audit(String id, String auditorId, String auditResult, String remark, String rejectReason);
}
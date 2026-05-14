package com.platform.admin.modules.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.modules.audit.entity.AuditQueueEntity;
import com.platform.admin.modules.audit.entity.AuditRuleEntity;

import java.util.List;
import java.util.Map;

public interface AuditService {

    IPage<AuditQueueEntity> getAuditQueue(Page<AuditQueueEntity> page, String contentType, String status);

    AuditQueueEntity getAuditDetail(String objectId);

    void approve(String objectId, String remark);

    void reject(String objectId, String reason, String remark);

    void batchApprove(List<String> objectIds, String remark);

    void batchReject(List<String> objectIds, String reason);

    List<AuditRuleEntity> getRules();

    AuditRuleEntity updateRules(AuditRuleEntity rule);

    Map<String, Object> getStatistics(String startDate, String endDate);
}
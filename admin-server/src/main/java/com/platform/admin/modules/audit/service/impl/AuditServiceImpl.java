package com.platform.admin.modules.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.BusinessException;
import com.platform.admin.modules.audit.entity.AuditQueueEntity;
import com.platform.admin.modules.audit.entity.AuditRuleEntity;
import com.platform.admin.modules.audit.mapper.AuditQueueMapper;
import com.platform.admin.modules.audit.mapper.AuditRuleMapper;
import com.platform.admin.modules.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditQueueMapper auditQueueMapper;

    @Autowired
    private AuditRuleMapper auditRuleMapper;

    @Override
    public IPage<AuditQueueEntity> getAuditQueue(Page<AuditQueueEntity> page, String contentType, String status) {
        LambdaQueryWrapper<AuditQueueEntity> wrapper = new LambdaQueryWrapper<>();
        if (contentType != null && !contentType.equals("ALL")) {
            wrapper.eq(AuditQueueEntity::getContentType, contentType);
        }
        if (status != null && !status.equals("ALL")) {
            wrapper.eq(AuditQueueEntity::getStatus, status);
        }
        wrapper.orderByDesc(AuditQueueEntity::getSubmitTime);
        return auditQueueMapper.selectPage(page, wrapper);
    }

    @Override
    public AuditQueueEntity getAuditDetail(String objectId) {
        return auditQueueMapper.selectById(objectId);
    }

    @Override
    @Transactional
    public void approve(String objectId, String remark) {
        AuditQueueEntity entity = auditQueueMapper.selectById(objectId);
        if (entity == null) {
            throw new BusinessException(3001, "待审核内容不存在");
        }
        entity.setStatus("APPROVED");
        entity.setAuditRemark(remark);
        entity.setAuditTime(LocalDateTime.now());
        auditQueueMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void reject(String objectId, String reason, String remark) {
        AuditQueueEntity entity = auditQueueMapper.selectById(objectId);
        if (entity == null) {
            throw new BusinessException(3001, "待审核内容不存在");
        }
        entity.setStatus("REJECTED");
        entity.setRejectReason(reason);
        entity.setAuditRemark(remark);
        entity.setAuditTime(LocalDateTime.now());
        auditQueueMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void batchApprove(List<String> objectIds, String remark) {
        for (String objectId : objectIds) {
            AuditQueueEntity entity = auditQueueMapper.selectById(objectId);
            if (entity != null) {
                entity.setStatus("APPROVED");
                entity.setAuditRemark(remark);
                entity.setAuditTime(LocalDateTime.now());
                auditQueueMapper.updateById(entity);
            }
        }
    }

    @Override
    @Transactional
    public void batchReject(List<String> objectIds, String reason) {
        for (String objectId : objectIds) {
            AuditQueueEntity entity = auditQueueMapper.selectById(objectId);
            if (entity != null) {
                entity.setStatus("REJECTED");
                entity.setRejectReason(reason);
                entity.setAuditTime(LocalDateTime.now());
                auditQueueMapper.updateById(entity);
            }
        }
    }

    @Override
    public List<AuditRuleEntity> getRules() {
        return auditRuleMapper.selectList(null);
    }

    @Override
    @Transactional
    public AuditRuleEntity updateRules(AuditRuleEntity rule) {
        auditRuleMapper.updateById(rule);
        return rule;
    }

    @Override
    public Map<String, Object> getStatistics(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        Map<String, Object> result = auditQueueMapper.getStatistics(start, end);
        if (result == null) {
            result = new HashMap<>();
        }
        
        long pendingCount = result.get("pendingCount") != null ? ((Number) result.get("pendingCount")).longValue() : 0;
        long approvedCount = result.get("approvedCount") != null ? ((Number) result.get("approvedCount")).longValue() : 0;
        long rejectedCount = result.get("rejectedCount") != null ? ((Number) result.get("rejectedCount")).longValue() : 0;
        result.put("totalSubmitted", pendingCount + approvedCount + rejectedCount);
        result.put("manualApproved", approvedCount);
        result.put("manualRejected", rejectedCount);
        
        return result;
    }
}
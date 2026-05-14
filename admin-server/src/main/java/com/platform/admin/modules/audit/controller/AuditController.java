package com.platform.admin.modules.audit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.PageResult;
import com.platform.admin.common.Result;
import com.platform.admin.modules.audit.entity.AuditQueueEntity;
import com.platform.admin.modules.audit.entity.AuditRuleEntity;
import com.platform.admin.modules.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/rules")
    public Result<List<AuditRuleEntity>> getRules() {
        List<AuditRuleEntity> rules = auditService.getRules();
        return Result.success(rules);
    }

    @PutMapping("/rules")
    public Result<AuditRuleEntity> updateRules(@RequestBody AuditRuleEntity rule) {
        AuditRuleEntity updated = auditService.updateRules(rule);
        return Result.success(updated);
    }

    @GetMapping("/queue")
    public Result<PageResult<AuditQueueEntity>> getAuditQueue(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "ALL") String status) {
        
        Page<AuditQueueEntity> pageParam = new Page<>(page, pageSize);
        IPage<AuditQueueEntity> queuePage = auditService.getAuditQueue(pageParam, type, status);
        
        PageResult<AuditQueueEntity> result = new PageResult<>(
                queuePage.getRecords(),
                queuePage.getTotal(),
                queuePage.getCurrent(),
                queuePage.getSize()
        );
        return Result.success(result);
    }

    @GetMapping("/queue/{objectId}")
    public Result<AuditQueueEntity> getAuditDetail(@PathVariable String objectId) {
        AuditQueueEntity detail = auditService.getAuditDetail(objectId);
        if (detail == null) {
            return Result.error(3001, "待审核内容不存在");
        }
        return Result.success(detail);
    }

    @PostMapping("/queue/{objectId}/approve")
    public Result<Void> approve(@PathVariable String objectId, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        auditService.approve(objectId, remark);
        return Result.success(null);
    }

    @PostMapping("/queue/{objectId}/reject")
    public Result<Void> reject(@PathVariable String objectId, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        String remark = body.get("remark");
        auditService.reject(objectId, reason, remark);
        return Result.success(null);
    }

    @PostMapping("/queue/batch-approve")
    public Result<Void> batchApprove(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> objectIds = (List<String>) body.get("objectIds");
        String remark = (String) body.get("remark");
        auditService.batchApprove(objectIds, remark);
        return Result.success(null);
    }

    @PostMapping("/queue/batch-reject")
    public Result<Void> batchReject(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> objectIds = (List<String>) body.get("objectIds");
        String reason = (String) body.get("reason");
        auditService.batchReject(objectIds, reason);
        return Result.success(null);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Map<String, Object> stats = auditService.getStatistics(startDate, endDate);
        return Result.success(stats);
    }
}
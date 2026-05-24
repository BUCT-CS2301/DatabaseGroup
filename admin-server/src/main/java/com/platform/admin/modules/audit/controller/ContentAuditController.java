package com.platform.admin.modules.audit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.audit.entity.ContentAudit;
import com.platform.admin.modules.audit.service.ContentAuditService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
public class ContentAuditController {

    private final ContentAuditService contentAuditService;

    public ContentAuditController(ContentAuditService contentAuditService) {
        this.contentAuditService = contentAuditService;
    }

    @GetMapping("/rules")
    public Result<Map<String, Object>> getAuditRules() {
        Map<String, Object> rules = new HashMap<>();
        rules.put("textAutoAuditEnabled", true);
        rules.put("imageAutoAuditEnabled", true);
        rules.put("sensitiveWordFilterEnabled", true);
        rules.put("textAuditAction", "REJECT");
        rules.put("imageAuditAction", "MANUAL");
        return Result.success(rules);
    }

    @PutMapping("/rules")
    public Result<Void> updateAuditRules(@RequestBody Map<String, Object> request) {
        return Result.success(null);
    }

    @GetMapping("/queue")
    public Result<Map<String, Object>> listQueue(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        Page<ContentAudit> pageRequest = new Page<>(page, pageSize);
        QueryWrapper<ContentAudit> wrapper = new QueryWrapper<>();
        if (type != null && !type.isEmpty() && !"ALL".equals(type)) {
            wrapper.eq("content_type", type);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("submit_time");
        IPage<ContentAudit> result = contentAuditService.page(pageRequest, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords().stream().map(audit -> {
            Map<String, Object> auditMap = new HashMap<>();
            auditMap.put("objectId", audit.getObjectId());
            auditMap.put("type", audit.getContentType());
            auditMap.put("content", audit.getContentText());
            Map<String, Object> author = new HashMap<>();
            author.put("objectId", audit.getAuthorId());
            auditMap.put("author", author);
            auditMap.put("autoAuditResult", audit.getAutoAuditResult());
            auditMap.put("status", audit.getStatus());
            auditMap.put("submitTime", audit.getSubmitTime());
            return auditMap;
        }).toList());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        
        return Result.success(data);
    }

    @GetMapping("/queue/{objectId}")
    public Result<Map<String, Object>> getAuditDetail(@PathVariable String objectId) {
        ContentAudit audit = contentAuditService.getById(objectId);
        if (audit == null) {
            return Result.error(3001, "待审核内容不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", audit.getObjectId());
        data.put("type", audit.getContentType());
        data.put("content", audit.getContentText());
        data.put("contentUrl", audit.getContentUrl());
        Map<String, Object> author = new HashMap<>();
        author.put("objectId", audit.getAuthorId());
        data.put("author", author);
        data.put("autoAuditResult", audit.getAutoAuditResult());
        data.put("autoAuditDetail", audit.getAutoAuditDetail());
        data.put("status", audit.getStatus());
        data.put("submitTime", audit.getSubmitTime());
        data.put("auditRemark", audit.getAuditRemark());
        data.put("rejectReason", audit.getRejectReason());
        
        return Result.success(data);
    }

    @PostMapping("/queue/{objectId}/approve")
    public Result<Void> approve(@PathVariable String objectId, @RequestBody(required = false) Map<String, String> request) {
        ContentAudit audit = contentAuditService.getById(objectId);
        if (audit == null) {
            return Result.error(3001, "待审核内容不存在");
        }
        
        String remark = request != null ? request.get("remark") : null;
        contentAuditService.audit(objectId, null, "PASS", remark, null);
        
        return Result.success(null);
    }

    @PostMapping("/queue/{objectId}/reject")
    public Result<Void> reject(@PathVariable String objectId, @RequestBody Map<String, String> request) {
        ContentAudit audit = contentAuditService.getById(objectId);
        if (audit == null) {
            return Result.error(3001, "待审核内容不存在");
        }
        
        String reason = request.get("reason");
        String remark = request.get("remark");
        contentAuditService.audit(objectId, null, "REJECT", remark, reason);
        
        return Result.success(null);
    }

    @PostMapping("/queue/batch-approve")
    public Result<Void> batchApprove(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> objectIds = (List<String>) request.get("objectIds");
        String remark = (String) request.get("remark");
        
        for (String objectId : objectIds) {
            contentAuditService.audit(objectId, null, "PASS", remark, null);
        }
        
        return Result.success(null);
    }

    @PostMapping("/queue/batch-reject")
    public Result<Void> batchReject(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> objectIds = (List<String>) request.get("objectIds");
        String reason = (String) request.get("reason");
        
        for (String objectId : objectIds) {
            contentAuditService.audit(objectId, null, "REJECT", null, reason);
        }
        
        return Result.success(null);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSubmitted", contentAuditService.count(new QueryWrapper<>()));
        stats.put("autoApproved", contentAuditService.count(new QueryWrapper<ContentAudit>().eq("auto_audit_result", "PASS")));
        stats.put("autoRejected", contentAuditService.count(new QueryWrapper<ContentAudit>().eq("auto_audit_result", "REJECT")));
        stats.put("manualApproved", contentAuditService.count(new QueryWrapper<ContentAudit>().eq("status", "APPROVED")));
        stats.put("manualRejected", contentAuditService.count(new QueryWrapper<ContentAudit>().eq("status", "REJECTED")));
        stats.put("pendingCount", contentAuditService.count(new QueryWrapper<ContentAudit>().eq("status", "PENDING")));
        
        return Result.success(stats);
    }

    @PostMapping("/queue")
    public Result<Map<String, Object>> submitForAudit(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        String contentType = (String) request.get("contentType");
        String authorId = (String) request.get("authorId");
        
        ContentAudit audit = contentAuditService.submitForAudit(content, contentType, authorId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", audit.getObjectId());
        data.put("type", audit.getContentType());
        data.put("content", audit.getContentText());
        data.put("status", audit.getStatus());
        data.put("submitTime", audit.getSubmitTime());
        
        return Result.success(data);
    }
}
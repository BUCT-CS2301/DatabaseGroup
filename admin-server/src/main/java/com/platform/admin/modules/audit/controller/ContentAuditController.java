package com.platform.admin.modules.audit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.audit.entity.ContentAudit;
import com.platform.admin.modules.audit.service.ContentAuditService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/content-audits")
public class ContentAuditController {

    private final ContentAuditService contentAuditService;

    public ContentAuditController(ContentAuditService contentAuditService) {
        this.contentAuditService = contentAuditService;
    }

    @GetMapping
    public Result<IPage<ContentAudit>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contentType) {
        Page<ContentAudit> pageRequest = new Page<>(page, size);
        QueryWrapper<ContentAudit> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        if (contentType != null && !contentType.isEmpty()) {
            wrapper.eq("content_type", contentType);
        }
        wrapper.orderByDesc("submit_time");
        IPage<ContentAudit> result = contentAuditService.page(pageRequest, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ContentAudit> getById(@PathVariable String id) {
        ContentAudit audit = contentAuditService.getById(id);
        if (audit == null) {
            return Result.error(ErrorCode.NOT_FOUND, "审核记录不存在");
        }
        return Result.success(audit);
    }

    @PostMapping
    public Result<ContentAudit> submit(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String contentType = request.get("contentType");
        String authorId = request.get("authorId");
        ContentAudit audit = contentAuditService.submitForAudit(content, contentType, authorId);
        return Result.success(audit);
    }

    @PutMapping("/{id}/audit")
    public Result<ContentAudit> audit(@PathVariable String id, @RequestBody Map<String, String> request) {
        String auditorId = request.get("auditorId");
        String auditResult = request.get("auditResult");
        String remark = request.get("remark");
        String rejectReason = request.get("rejectReason");
        ContentAudit audit = contentAuditService.audit(id, auditorId, auditResult, remark, rejectReason);
        if (audit == null) {
            return Result.error(ErrorCode.NOT_FOUND, "审核记录不存在");
        }
        return Result.success(audit);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        if (!contentAuditService.removeById(id)) {
            return Result.error(ErrorCode.NOT_FOUND, "审核记录不存在");
        }
        return Result.success(null);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", contentAuditService.count(new QueryWrapper<ContentAudit>().eq("status", "PENDING")));
        stats.put("approved", contentAuditService.count(new QueryWrapper<ContentAudit>().eq("status", "APPROVED")));
        stats.put("rejected", contentAuditService.count(new QueryWrapper<ContentAudit>().eq("status", "REJECTED")));
        return Result.success(stats);
    }
}
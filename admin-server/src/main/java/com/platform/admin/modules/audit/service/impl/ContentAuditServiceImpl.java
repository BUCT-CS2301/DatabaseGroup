package com.platform.admin.modules.audit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.admin.modules.audit.entity.ContentAudit;
import com.platform.admin.modules.audit.mapper.ContentAuditMapper;
import com.platform.admin.modules.audit.service.ContentAuditService;
import com.platform.admin.modules.system.service.SensitiveWordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContentAuditServiceImpl extends ServiceImpl<ContentAuditMapper, ContentAudit> implements ContentAuditService {

    private final SensitiveWordService sensitiveWordService;

    public ContentAuditServiceImpl(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @Override
    public ContentAudit submitForAudit(String content, String contentType, String authorId) {
        ContentAudit audit = new ContentAudit();
        audit.setObjectId(UUID.randomUUID().toString());
        audit.setContentText(content);
        audit.setContentType(contentType);
        audit.setAuthorId(authorId);
        audit.setSubmitTime(LocalDateTime.now());
        
        List<String> foundWords = new ArrayList<>();
        var words = sensitiveWordService.getAllWords();
        for (var word : words) {
            if (content.contains(word.getWord())) {
                foundWords.add(word.getWord());
            }
        }
        
        if (!foundWords.isEmpty()) {
            audit.setAutoAuditResult("MANUAL");
            audit.setAutoAuditDetail("检测到敏感词: " + String.join(",", foundWords));
            audit.setStatus("PENDING");
        } else {
            audit.setAutoAuditResult("PASS");
            audit.setAutoAuditDetail("自动审核通过");
            audit.setStatus("PENDING");
        }
        
        save(audit);
        return audit;
    }

    @Override
    public ContentAudit audit(String id, String auditorId, String auditResult, String remark, String rejectReason) {
        ContentAudit audit = getById(id);
        if (audit != null) {
            audit.setAuditUserId(auditorId);
            audit.setAuditRemark(remark);
            audit.setAuditTime(LocalDateTime.now());
            if ("REJECT".equals(auditResult)) {
                audit.setStatus("REJECTED");
                audit.setRejectReason(rejectReason);
            } else {
                audit.setStatus("APPROVED");
            }
            updateById(audit);
        }
        return audit;
    }
}
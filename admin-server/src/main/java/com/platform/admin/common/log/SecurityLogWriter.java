package com.platform.admin.common.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.admin.modules.log.entity.SecurityLogEntity;
import com.platform.admin.modules.log.mapper.SecurityLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 统一安全日志写入组件，写入失败不影响主流程。
 */
@Component
public class SecurityLogWriter {

    private static final Logger log = LoggerFactory.getLogger(SecurityLogWriter.class);
    private static final int MAX_RETRY = 3;

    private final SecurityLogMapper securityLogMapper;
    private final ObjectMapper objectMapper;

    public SecurityLogWriter(SecurityLogMapper securityLogMapper, ObjectMapper objectMapper) {
        this.securityLogMapper = securityLogMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 异步写入安全日志。
     */
    @Async
    public void writeAsync(String userId, String eventType, String ipAddress, Map<String, Object> detail) {
        writeWithRetry(userId, eventType, ipAddress, detail);
    }

    /**
     * 同步写入安全日志（供鉴权链路使用）。
     */
    public void write(String userId, String eventType, String ipAddress, Map<String, Object> detail) {
        writeWithRetry(userId, eventType, ipAddress, detail);
    }

    /**
     * 记录鉴权拒绝事件。
     */
    public void writeAccessDenied(String userId, String ipAddress, String path, String httpMethod, String denyReason) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("path", path);
        detail.put("httpMethod", httpMethod);
        detail.put("denyReason", denyReason);
        detail.put("result", "DENIED");
        write(userId, SecurityLogEventType.ACCESS_DENIED, ipAddress, detail);
    }

    /**
     * 记录安全模块访问事件。
     */
    public void writeSecurityAccess(
            String userId,
            String eventType,
            String ipAddress,
            String path,
            String httpMethod,
            String result,
            Map<String, Object> extra
    ) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("path", path);
        detail.put("httpMethod", httpMethod);
        detail.put("result", result);
        if (extra != null && !extra.isEmpty()) {
            detail.putAll(extra);
        }
        writeAsync(userId, eventType, ipAddress, detail);
    }

    private void writeWithRetry(String userId, String eventType, String ipAddress, Map<String, Object> detail) {
        String detailJson = serializeDetail(detail);
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                SecurityLogEntity entity = SecurityLogEntity.builder()
                        .objectId(UUID.randomUUID().toString())
                        .userId(userId)
                        .eventType(eventType)
                        .detail(detailJson)
                        .ipAddress(ipAddress)
                        .createTime(LocalDateTime.now())
                        .build();
                securityLogMapper.insert(entity);
                return;
            } catch (Exception ex) {
                if (attempt == MAX_RETRY) {
                    log.warn("security_log write failed after {} attempts eventType={}", MAX_RETRY, eventType, ex);
                }
            }
        }
    }

    private String serializeDetail(Map<String, Object> detail) {
        if (detail == null || detail.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(SensitiveDataSanitizer.sanitize(detail));
        } catch (JsonProcessingException ex) {
            return detail.toString();
        }
    }
}

package com.platform.admin.common.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.admin.modules.log.entity.OperationLogEntity;
import com.platform.admin.modules.log.mapper.LogOperationLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 统一操作日志写入组件，供非安全数据 CUD 场景调用。
 */
@Component
public class OperationLogWriter {

    private static final Logger log = LoggerFactory.getLogger(OperationLogWriter.class);
    private static final int MAX_RETRY = 3;

    private final LogOperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    public OperationLogWriter(LogOperationLogMapper operationLogMapper, ObjectMapper objectMapper) {
        this.operationLogMapper = operationLogMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 异步写入操作日志，失败不影响主流程。
     */
    @Async
    public void writeAsync(
            String userId,
            String ipAddress,
            String module,
            String action,
            String targetType,
            String targetId,
            Map<String, ?> detail
    ) {
        writeWithRetry(userId, ipAddress, module, action, targetType, targetId, detail);
    }

    private void writeWithRetry(
            String userId,
            String ipAddress,
            String module,
            String action,
            String targetType,
            String targetId,
            Map<String, ?> detail
    ) {
        String detailJson = serializeDetail(detail);
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                OperationLogEntity entity = OperationLogEntity.builder()
                        .objectId(UUID.randomUUID().toString())
                        .userId(userId)
                        .module(module)
                        .action(action)
                        .targetType(targetType)
                        .targetId(targetId)
                        .detail(detailJson)
                        .ipAddress(ipAddress)
                        .createTime(LocalDateTime.now())
                        .build();
                operationLogMapper.insert(entity);
                return;
            } catch (Exception ex) {
                if (attempt == MAX_RETRY) {
                    log.warn("operation_log write failed after {} attempts module={} action={}",
                            MAX_RETRY, module, action, ex);
                }
            }
        }
    }

    private String serializeDetail(Map<String, ?> detail) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (detail != null) {
            payload.putAll(SensitiveDataSanitizer.sanitize(detail));
        }
        if (!payload.containsKey("result")) {
            payload.put("result", "SUCCESS");
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return payload.toString();
        }
    }
}

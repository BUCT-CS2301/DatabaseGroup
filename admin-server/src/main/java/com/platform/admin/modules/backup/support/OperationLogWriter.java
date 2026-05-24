package com.platform.admin.modules.backup.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.admin.modules.backup.entity.OperationLogEntity;
import com.platform.admin.modules.backup.mapper.OperationLogMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class OperationLogWriter {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    public OperationLogWriter(OperationLogMapper operationLogMapper, ObjectMapper objectMapper) {
        this.operationLogMapper = operationLogMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 写入操作日志。
     */
    public void write(String userId, String module, String action, String targetType, String targetId, Map<String, ?> detail) {
        String detailJson = null;
        if (detail != null && !detail.isEmpty()) {
            try {
                detailJson = objectMapper.writeValueAsString(detail);
            } catch (JsonProcessingException ignored) {
                detailJson = detail.toString();
            }
        }
        OperationLogEntity entity = OperationLogEntity.builder()
                .objectId(UUID.randomUUID().toString())
                .userId(userId)
                .module(module)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detailJson)
                .createTime(LocalDateTime.now())
                .build();
        operationLogMapper.insert(entity);
    }
}

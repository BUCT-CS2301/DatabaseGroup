package com.platform.admin.modules.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.admin.modules.log.entity.SecurityLogEntity;
import com.platform.admin.modules.log.mapper.SecurityLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 记录日志模块鉴权失败事件到 security_log。
 */
@Service
public class LogSecurityAuditService {

    private static final Logger log = LoggerFactory.getLogger(LogSecurityAuditService.class);

    private final SecurityLogMapper securityLogMapper;
    private final ObjectMapper objectMapper;

    public LogSecurityAuditService(SecurityLogMapper securityLogMapper, ObjectMapper objectMapper) {
        this.securityLogMapper = securityLogMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 写入鉴权拒绝安全日志。
     *
     * @param userId     用户 ID，未登录可为 null
     * @param ipAddress  客户端 IP
     * @param path       请求路径
     * @param denyReason 拒绝原因（如 UNAUTHORIZED / FORBIDDEN）
     */
    public void recordAccessDenied(String userId, String ipAddress, String path, String denyReason) {
        try {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("denyReason", denyReason);
            detail.put("path", path);
            SecurityLogEntity entity = SecurityLogEntity.builder()
                    .objectId(UUID.randomUUID().toString())
                    .userId(userId)
                    .eventType("ACCESS_DENIED")
                    .detail(objectMapper.writeValueAsString(detail))
                    .ipAddress(ipAddress)
                    .createTime(LocalDateTime.now())
                    .build();
            securityLogMapper.insert(entity);
        } catch (JsonProcessingException ex) {
            log.warn("failed to persist log access denied security event path={}", path, ex);
        } catch (Exception ex) {
            log.warn("failed to persist log access denied security event path={}", path, ex);
        }
    }
}

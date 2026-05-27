package com.platform.admin.modules.log.service;

import com.platform.admin.common.log.SecurityLogWriter;
import org.springframework.stereotype.Service;

/**
 * 日志模块鉴权审计，委托统一 SecurityLogWriter。
 */
@Service
public class LogSecurityAuditService {

    private final SecurityLogWriter securityLogWriter;

    public LogSecurityAuditService(SecurityLogWriter securityLogWriter) {
        this.securityLogWriter = securityLogWriter;
    }

    /**
     * 写入鉴权拒绝安全日志。
     */
    public void recordAccessDenied(String userId, String ipAddress, String path, String denyReason) {
        securityLogWriter.writeAccessDenied(userId, ipAddress, path, null, denyReason);
    }
}

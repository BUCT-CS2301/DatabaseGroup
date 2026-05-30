package com.platform.admin.modules.log.support;

import com.platform.admin.common.log.SecurityLogEventType;
import com.platform.admin.common.log.SecurityLogWriter;
import com.platform.admin.common.util.ClientIpUtils;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * 日志模块成功访问后写入 security_log。
 */
@Aspect
@Component
public class LogSecurityAuditAspect {

    private final SecurityLogWriter securityLogWriter;
    private final SecurityUtil securityUtil;

    public LogSecurityAuditAspect(SecurityLogWriter securityLogWriter, SecurityUtil securityUtil) {
        this.securityLogWriter = securityLogWriter;
        this.securityUtil = securityUtil;
    }

    @AfterReturning("execution(* com.platform.admin.modules.log.controller.LogController.pageOperationLogs(..))")
    public void afterOperationList(JoinPoint joinPoint) {
        auditLogQuery(SecurityLogEventType.LOG_QUERY, "/api/v1/logs/operation", "GET",
                Map.of("scope", "OPERATION_LIST"));
    }

    @AfterReturning("execution(* com.platform.admin.modules.log.controller.LogController.getOperationLogDetail(..))")
    public void afterOperationDetail(JoinPoint joinPoint) {
        Object objectId = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
        auditLogQuery(SecurityLogEventType.LOG_QUERY, "/api/v1/logs/operation/" + objectId, "GET",
                Map.of("scope", "OPERATION_DETAIL", "objectId", objectId));
    }

    @AfterReturning("execution(* com.platform.admin.modules.log.controller.LogController.pageSystemLogs(..))")
    public void afterSystemList() {
        auditLogQuery(SecurityLogEventType.LOG_QUERY, "/api/v1/logs/system", "GET",
                Map.of("scope", "SYSTEM_LIST"));
    }

    @AfterReturning("execution(* com.platform.admin.modules.log.controller.LogController.pageSecurityLogs(..))")
    public void afterSecurityList() {
        auditLogQuery(SecurityLogEventType.LOG_QUERY, "/api/v1/logs/security", "GET",
                Map.of("scope", "SECURITY_LIST"));
    }

    @AfterReturning("execution(* com.platform.admin.modules.log.controller.LogController.exportLogs(..))")
    public void afterExport(JoinPoint joinPoint) {
        auditLogQuery(SecurityLogEventType.LOG_EXPORT, "/api/v1/logs/export", "POST",
                Map.of("scope", "EXPORT"));
    }

    @AfterReturning("execution(* com.platform.admin.modules.log.controller.LogController.downloadExport(..))")
    public void afterDownload(JoinPoint joinPoint) {
        Object fileId = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
        auditLogQuery(SecurityLogEventType.LOG_DOWNLOAD, "/api/v1/logs/download", "GET",
                Map.of("scope", "DOWNLOAD", "fileId", fileId));
    }

    private void auditLogQuery(String eventType, String path, String httpMethod, Map<String, Object> extra) {
        try {
            AuthUser user = securityUtil.getCurrentUser();
            HttpServletRequest request = currentRequest();
            securityLogWriter.writeSecurityAccess(
                    user.objectId(),
                    eventType,
                    ClientIpUtils.resolve(request),
                    path,
                    httpMethod,
                    "SUCCESS",
                    extra
            );
        } catch (Exception ignored) {
            // 审计失败不影响主流程
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}

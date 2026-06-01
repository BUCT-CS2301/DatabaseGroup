package com.platform.admin.modules.log.vo;

public class LogStatsVO {

    private Integer operationCount;

    private Integer loginCount;

    private Integer errorCount;

    private Integer securityCount;

    public LogStatsVO() {
    }

    public LogStatsVO(Integer operationCount, Integer loginCount, Integer errorCount, Integer securityCount) {
        this.operationCount = operationCount;
        this.loginCount = loginCount;
        this.errorCount = errorCount;
        this.securityCount = securityCount;
    }

    public Integer getOperationCount() {
        return operationCount;
    }

    public void setOperationCount(Integer operationCount) {
        this.operationCount = operationCount;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getSecurityCount() {
        return securityCount;
    }

    public void setSecurityCount(Integer securityCount) {
        this.securityCount = securityCount;
    }
}
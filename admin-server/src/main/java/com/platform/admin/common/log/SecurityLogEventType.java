package com.platform.admin.common.log;

public final class SecurityLogEventType {

    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String REGISTER = "REGISTER";
    public static final String LOG_QUERY = "LOG_QUERY";
    public static final String LOG_EXPORT = "LOG_EXPORT";
    public static final String LOG_DOWNLOAD = "LOG_DOWNLOAD";
    public static final String BACKUP_MANUAL = "BACKUP_MANUAL";
    public static final String BACKUP_RESTORE = "BACKUP_RESTORE";
    public static final String BACKUP_QUERY = "BACKUP_QUERY";
    public static final String BACKUP_SCHEDULE_CREATE = "BACKUP_SCHEDULE_CREATE";
    public static final String BACKUP_SCHEDULE_UPDATE = "BACKUP_SCHEDULE_UPDATE";
    public static final String BACKUP_SCHEDULE_DELETE = "BACKUP_SCHEDULE_DELETE";
    public static final String BACKUP_RECORD_DELETE = "BACKUP_RECORD_DELETE";

    private SecurityLogEventType() {
    }
}

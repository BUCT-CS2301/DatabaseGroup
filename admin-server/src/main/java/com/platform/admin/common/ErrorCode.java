package com.platform.admin.common;

public class ErrorCode {
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    /** 资源冲突（如外键约束禁止删除） */
    public static final int CONFLICT = 409;
    public static final int INTERNAL_ERROR = 500;
}

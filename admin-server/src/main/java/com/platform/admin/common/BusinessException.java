package com.platform.admin.common;

/**
 * 业务异常，统一携带错误码和错误信息。
 */
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

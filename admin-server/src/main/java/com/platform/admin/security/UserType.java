package com.platform.admin.security;

/**
 * 与 {@code user.user_type} / {@code role.role_code} 对齐的后台角色编码。
 */
public enum UserType {
    ADMIN,
    AUDITOR,
    USER,
    KNOWLEDGE_SERVICE,
    MOBILE;

    public static UserType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        try {
            return UserType.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return USER;
        }
    }
}

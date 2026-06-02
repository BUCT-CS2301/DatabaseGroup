package com.platform.admin.security;

public enum UserType {
    ADMIN,
    KNOWLEDGE_SERVICE,
    MOBILE;

    public static UserType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return MOBILE;
        }
        try {
            return UserType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return MOBILE;
        }
    }
}

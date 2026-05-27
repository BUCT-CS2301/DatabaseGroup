package com.platform.admin.common.log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SensitiveDataSanitizer {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwordhash", "password_hash", "token", "accesstoken",
            "access_token", "refreshtoken", "refresh_token", "authorization"
    );

    private SensitiveDataSanitizer() {
    }

    /**
     * 对 Map 中的敏感字段脱敏。
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> sanitize(Map<String, ?> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (isSensitiveKey(key)) {
                sanitized.put(key, "***");
            } else if (value instanceof Map<?, ?> nested) {
                Map<String, Object> nestedMap = new LinkedHashMap<>();
                nested.forEach((k, v) -> nestedMap.put(String.valueOf(k), v));
                sanitized.put(key, sanitize(nestedMap));
            } else {
                sanitized.put(key, value);
            }
        });
        return sanitized;
    }

    private static boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEYS.contains(key.toLowerCase());
    }
}

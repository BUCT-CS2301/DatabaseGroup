package com.platform.admin.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PublicUrlResolver {

    @Value("${file.public-base-url:}")
    private String publicBaseUrl;

    public String toPublicUrl(String value, HttpServletRequest request) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String path = value.trim();

        // 如果数据库里以前已经存了 http://localhost:8080/uploads/xxx，
        // 这里自动截取 /uploads/xxx，再重新拼接当前环境地址。
        int uploadIndex = path.indexOf("/uploads/");
        if (uploadIndex >= 0) {
            path = path.substring(uploadIndex);
        } else if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return resolveBaseUrl(request) + path;
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        if (StringUtils.hasText(publicBaseUrl)) {
            return trimTrailingSlash(publicBaseUrl);
        }

        String scheme = request.getHeader("X-Forwarded-Proto");
        if (!StringUtils.hasText(scheme)) {
            scheme = request.getScheme();
        }

        String host = request.getHeader("Host");
        if (!StringUtils.hasText(host)) {
            host = request.getServerName() + ":" + request.getServerPort();
        }

        return trimTrailingSlash(scheme + "://" + host);
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
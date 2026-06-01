package com.platform.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文物图片公网 URL 拼接配置（{@code artifact_image.file_name} → HTTP(S) URL）。
 */
@ConfigurationProperties(prefix = "app.relics")
public class RelicAutoImageProperties {

    /**
     * 对外访问基址，用于拼接图片 URL；各环境通过配置区分，禁止硬编码生产域名。
     */
    private String imagePublicBaseUrl = "";

    public String getImagePublicBaseUrl() {
        return imagePublicBaseUrl;
    }

    public void setImagePublicBaseUrl(String imagePublicBaseUrl) {
        this.imagePublicBaseUrl = imagePublicBaseUrl == null ? "" : imagePublicBaseUrl;
    }
}

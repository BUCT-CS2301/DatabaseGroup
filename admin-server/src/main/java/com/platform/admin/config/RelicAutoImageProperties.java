package com.platform.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 创建文物时自动生成 {@code image_path} / {@code image_url} 的规则（PRD §2.4）。
 */
@ConfigurationProperties(prefix = "app.relics")
public class RelicAutoImageProperties {

    /**
     * 创建时写入路径的预期扩展名（小写、不含点），默认 {@code jpg}。
     */
    private String defaultImageExtension = "jpg";

    /**
     * 对外访问基址，用于拼接 {@code image_url}；各环境通过配置区分，禁止硬编码生产域名。
     */
    private String imagePublicBaseUrl = "";

    public String getDefaultImageExtension() {
        return defaultImageExtension;
    }

    public void setDefaultImageExtension(String defaultImageExtension) {
        this.defaultImageExtension = defaultImageExtension == null ? "jpg" : defaultImageExtension;
    }

    public String getImagePublicBaseUrl() {
        return imagePublicBaseUrl;
    }

    public void setImagePublicBaseUrl(String imagePublicBaseUrl) {
        this.imagePublicBaseUrl = imagePublicBaseUrl == null ? "" : imagePublicBaseUrl;
    }
}

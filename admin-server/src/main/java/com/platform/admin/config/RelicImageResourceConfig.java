package com.platform.admin.config;

import com.platform.admin.modules.artifact.support.RelicImageStorage;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将落盘目录映射为 {@code /relics-images/**}，与 {@code app.relics.image-public-base-url} 拼接的 URL 一致。
 */
@Configuration
public class RelicImageResourceConfig implements WebMvcConfigurer {

    private final RelicImageStorage relicImageStorage;

    public RelicImageResourceConfig(RelicImageStorage relicImageStorage) {
        this.relicImageStorage = relicImageStorage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = relicImageStorage.getRoot().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/relics-images/**")
                .addResourceLocations(location);
    }
}

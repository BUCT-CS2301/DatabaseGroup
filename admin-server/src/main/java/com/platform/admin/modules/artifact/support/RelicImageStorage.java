package com.platform.admin.modules.artifact.support;

import com.platform.admin.config.RelicImageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 解析文物图片物理根目录（PRD：默认 {@code src/main/resources/relics-images} 对应 classpath）。
 */
@Component
public class RelicImageStorage {

    private static final Logger log = LoggerFactory.getLogger(RelicImageStorage.class);

    private final RelicImageProperties properties;
    private Path resolvedRoot;

    public RelicImageStorage(RelicImageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() throws IOException {
        this.resolvedRoot = resolveRoot();
        Files.createDirectories(resolvedRoot);
        log.info("relic image storage root={}", resolvedRoot);
    }

    public Path getRoot() {
        return resolvedRoot;
    }

    private Path resolveRoot() throws IOException {
        if (StringUtils.hasText(properties.getDirectory())) {
            Path p = Paths.get(properties.getDirectory());
            return p.toAbsolutePath().normalize();
        }
        ClassPathResource cpr = new ClassPathResource("relics-images");
        try {
            return cpr.getFile().toPath().toAbsolutePath().normalize();
        } catch (IOException ex) {
            Path fallback = Paths.get(System.getProperty("java.io.tmpdir"), "admin-server-relics-images")
                    .toAbsolutePath()
                    .normalize();
            log.warn("classpath relics-images is not a writable directory (e.g. running from jar); using {}", fallback);
            return fallback;
        }
    }
}

package com.platform.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文物图片落盘目录配置；见 PRD「存储目录与文件命名」。
 */
@ConfigurationProperties(prefix = "app.relic-images")
public class RelicImageProperties {

    /**
     * 绝对路径或相对运行目录的路径；为空时优先使用 classpath 下 {@code relics-images}（开发 exploded），
     * 否则回退到临时目录（例如 fat jar 运行时）。
     */
    private String directory = "";

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory == null ? "" : directory;
    }
}

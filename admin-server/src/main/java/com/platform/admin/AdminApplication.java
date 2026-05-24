package com.platform.admin;

import com.platform.admin.config.BackupProperties;
import com.platform.admin.config.RelicAutoImageProperties;
import com.platform.admin.config.RelicImageProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({RelicImageProperties.class, RelicAutoImageProperties.class, BackupProperties.class})
@MapperScan({
        "com.platform.admin.modules.artifact.mapper",
        "com.platform.admin.modules.museum.mapper",
        "com.platform.admin.modules.backup.mapper"
})
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}

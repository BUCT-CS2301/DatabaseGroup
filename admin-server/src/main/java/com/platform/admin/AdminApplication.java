package com.platform.admin;

import com.platform.admin.config.BackupProperties;
import com.platform.admin.config.RelicAutoImageProperties;
import com.platform.admin.config.RelicImageProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({RelicImageProperties.class, RelicAutoImageProperties.class, BackupProperties.class})
@MapperScan({"com.platform.admin.modules.artifact.mapper", "com.platform.admin.modules.museum.mapper", "com.platform.admin.modules.system.mapper", "com.platform.admin.modules.audit.mapper", "com.platform.admin.modules.role.mapper", "com.platform.admin.modules.user.mapper", "com.platform.admin.modules.backup.mapper", "com.platform.admin.modules.log.mapper"})
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}

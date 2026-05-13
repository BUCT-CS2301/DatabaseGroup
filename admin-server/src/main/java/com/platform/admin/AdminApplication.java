package com.platform.admin;

import com.platform.admin.config.RelicAutoImageProperties;
import com.platform.admin.config.RelicImageProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({RelicImageProperties.class, RelicAutoImageProperties.class})
@MapperScan({"com.platform.admin.modules.artifact.mapper", "com.platform.admin.modules.museum.mapper"})
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}

package com.platform.admin.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.platform.admin.common.log.DatabaseSystemLogAppender;
import com.platform.admin.common.log.SystemLogWriter;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemLogConfig {

    private final SystemLogWriter systemLogWriter;

    public SystemLogConfig(SystemLogWriter systemLogWriter) {
        this.systemLogWriter = systemLogWriter;
    }

    /**
     * 注册 DB Appender，将应用 INFO/WARN/ERROR 写入 system_log。
     */
    @PostConstruct
    public void registerDatabaseAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        DatabaseSystemLogAppender appender = new DatabaseSystemLogAppender(systemLogWriter);
        appender.setContext(context);
        appender.setName("DATABASE");
        appender.start();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
    }
}

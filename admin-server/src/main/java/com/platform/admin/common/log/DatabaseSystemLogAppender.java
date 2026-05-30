package com.platform.admin.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * 将 INFO/WARN/ERROR 日志异步写入 system_log。
 */
public class DatabaseSystemLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String SKIP_LOGGER_PREFIX = "com.platform.admin.common.log";

    private final SystemLogWriter systemLogWriter;

    public DatabaseSystemLogAppender(SystemLogWriter systemLogWriter) {
        this.systemLogWriter = systemLogWriter;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event.getLoggerName() != null && event.getLoggerName().startsWith(SKIP_LOGGER_PREFIX)) {
            return;
        }
        Level level = event.getLevel();
        if (level.toInt() < Level.INFO_INT) {
            return;
        }
        String levelName = level.toString();
        if (!"INFO".equals(levelName) && !"WARN".equals(levelName) && !"ERROR".equals(levelName)) {
            return;
        }
        String exception = null;
        if (event.getThrowableProxy() != null) {
            exception = ThrowableProxyUtil.asString(event.getThrowableProxy());
        }
        systemLogWriter.enqueue(levelName, event.getFormattedMessage(), exception);
    }
}

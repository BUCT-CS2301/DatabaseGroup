package com.platform.admin.common.log;

import com.platform.admin.modules.log.entity.SystemLogEntity;
import com.platform.admin.modules.log.mapper.SystemLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 异步写入 system_log，避免阻塞业务线程。
 */
@Component
public class SystemLogWriter {

    private static final Logger log = LoggerFactory.getLogger(SystemLogWriter.class);
    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final int MAX_EXCEPTION_LENGTH = 4000;
    private static final String DEFAULT_MODULE = "admin-server";

    private final SystemLogMapper systemLogMapper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "system-log-writer");
        thread.setDaemon(true);
        return thread;
    });
    private final ArrayBlockingQueue<SystemLogEntity> queue = new ArrayBlockingQueue<>(1000);
    private volatile boolean running = false;

    public SystemLogWriter(SystemLogMapper systemLogMapper) {
        this.systemLogMapper = systemLogMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        running = true;
        executor.submit(this::drainQueue);
    }

    /**
     * 入队系统日志，队列满时丢弃并计数。
     */
    public void enqueue(String level, String message, String exception) {
        SystemLogEntity entity = SystemLogEntity.builder()
                .objectId(UUID.randomUUID().toString())
                .level(level)
                .module(DEFAULT_MODULE)
                .message(truncate(message, MAX_MESSAGE_LENGTH))
                .exception(truncate(exception, MAX_EXCEPTION_LENGTH))
                .createTime(LocalDateTime.now())
                .build();
        if (!queue.offer(entity)) {
            log.warn("system_log queue full, dropping log level={}", level);
        }
    }

    private void drainQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SystemLogEntity entity = queue.poll(500, TimeUnit.MILLISECONDS);
                if (entity == null) {
                    continue;
                }
                systemLogMapper.insert(entity);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.warn("system_log write failed", ex);
            }
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

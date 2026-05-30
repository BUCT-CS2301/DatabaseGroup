package com.platform.admin.modules.backup.support;

import com.platform.admin.modules.backup.enums.RestoreTaskStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RestoreTaskStore {

    private static final String KEY_PREFIX = "backup:restore:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;

    public RestoreTaskStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String createTask(String backupRecordId) {
        String taskId = UUID.randomUUID().toString();
        Map<String, Object> payload = new HashMap<>();
        payload.put("restoreTaskId", taskId);
        payload.put("backupRecordId", backupRecordId);
        payload.put("status", RestoreTaskStatus.PROCESSING.name());
        redisTemplate.opsForValue().set(KEY_PREFIX + taskId, payload, TTL);
        return taskId;
    }

    public Map<String, Object> getTask(String restoreTaskId) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + restoreTaskId);
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new HashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return null;
    }

    public void updateStatus(String restoreTaskId, RestoreTaskStatus status) {
        Map<String, Object> task = getTask(restoreTaskId);
        if (task == null) {
            return;
        }
        task.put("status", status.name());
        redisTemplate.opsForValue().set(KEY_PREFIX + restoreTaskId, task, TTL);
    }

    public boolean hasProcessingTask() {
        // 扫描所有 restore key 成本较高；维护一个全局锁 key 更轻量
        Boolean exists = redisTemplate.hasKey(KEY_PREFIX + "active");
        return Boolean.TRUE.equals(exists);
    }

    public void markRestoreActive(String restoreTaskId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + "active", restoreTaskId, TTL);
    }

    public void clearRestoreActive() {
        redisTemplate.delete(KEY_PREFIX + "active");
    }
}

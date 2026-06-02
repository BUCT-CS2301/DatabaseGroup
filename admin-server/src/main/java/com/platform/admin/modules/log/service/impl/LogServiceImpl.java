package com.platform.admin.modules.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PageResult;
import com.platform.admin.modules.log.dto.LogExportRequest;
import com.platform.admin.modules.log.entity.OperationLogEntity;
import com.platform.admin.modules.log.entity.SecurityLogEntity;
import com.platform.admin.modules.log.entity.SystemLogEntity;
import com.platform.admin.modules.log.mapper.LogOperationLogMapper;
import com.platform.admin.modules.log.mapper.SecurityLogMapper;
import com.platform.admin.modules.log.mapper.SystemLogMapper;
import com.platform.admin.modules.log.service.LogService;
import com.platform.admin.modules.log.support.LogExportFileRegistry;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.SecurityUtil;
import com.platform.admin.modules.log.vo.LogExportVO;
import com.platform.admin.modules.log.vo.LogStatsVO;
import com.platform.admin.modules.log.vo.OperationLogDetailVO;
import com.platform.admin.modules.log.vo.OperationLogVO;
import com.platform.admin.modules.log.vo.SecurityLogVO;
import com.platform.admin.modules.log.vo.SystemLogVO;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {
    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 20L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final int LOG_EXPORT_FAILED_CODE = 5001;
    private static final long EXPORT_DEBOUNCE_MS = 1000L;

    private final LogOperationLogMapper operationLogMapper;
    private final SystemLogMapper systemLogMapper;
    private final SecurityLogMapper securityLogMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final LogExportFileRegistry exportFileRegistry;
    private final SecurityUtil securityUtil;
    private final ConcurrentHashMap<String, Long> exportDebounceMap = new ConcurrentHashMap<>();

    public LogServiceImpl(LogOperationLogMapper operationLogMapper,
                          SystemLogMapper systemLogMapper,
                          SecurityLogMapper securityLogMapper,
                          UserMapper userMapper,
                          ObjectMapper objectMapper,
                          LogExportFileRegistry exportFileRegistry,
                          SecurityUtil securityUtil) {
        this.operationLogMapper = operationLogMapper;
        this.systemLogMapper = systemLogMapper;
        this.securityLogMapper = securityLogMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.exportFileRegistry = exportFileRegistry;
        this.securityUtil = securityUtil;
    }

    @Override
    public PageResult<OperationLogVO> pageOperationLogs(long page, long pageSize, String userId, String module,
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        validateTimeRange(startTime, endTime);
        long safePage = normalizePage(page);
        long safePageSize = normalizePageSize(pageSize);
        LocalDateTime safeEnd = endTime == null ? LocalDateTime.now() : endTime;
        LocalDateTime safeStart = startTime == null ? safeEnd.minusHours(24) : startTime;

        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(OperationLogEntity::getCreateTime, safeStart)
                .le(OperationLogEntity::getCreateTime, safeEnd)
                .orderByDesc(OperationLogEntity::getCreateTime);
        if (StringUtils.hasText(userId)) {
            wrapper.eq(OperationLogEntity::getUserId, userId);
        }
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLogEntity::getModule, module);
        }

        Page<OperationLogEntity> result = operationLogMapper.selectPage(new Page<>(safePage, safePageSize), wrapper);
        Map<String, String> userDisplayMap = loadUserDisplayMap(result.getRecords().stream()
                .map(OperationLogEntity::getUserId)
                .filter(StringUtils::hasText)
                .toList());

        List<OperationLogVO> records = result.getRecords().stream()
                .map(item -> {
                    Map<String, Object> detail = parseJsonObject(item.getDetail());
                    return new OperationLogVO(
                            item.getObjectId(),
                            item.getUserId(),
                            userDisplayMap.getOrDefault(item.getUserId(), item.getUserId()),
                            item.getModule(),
                            item.getAction(),
                            resolveOperationResult(detail),
                            item.getCreateTime()
                    );
                })
                .toList();
        return new PageResult<>(records, result.getTotal(), safePage, safePageSize);
    }

    @Override
    public OperationLogDetailVO getOperationLogDetail(String objectId) {
        OperationLogEntity entity = operationLogMapper.selectById(objectId);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        Map<String, Object> detail = parseJsonObject(entity.getDetail());
        Map<String, String> userDisplayMap = loadUserDisplayMap(List.of(entity.getUserId()));
        return new OperationLogDetailVO(
                entity.getObjectId(),
                entity.getUserId(),
                userDisplayMap.getOrDefault(entity.getUserId(), entity.getUserId()),
                entity.getModule(),
                entity.getAction(),
                resolveOperationResult(detail),
                entity.getIpAddress(),
                entity.getCreateTime(),
                resolveSubMap(detail, "requestParams"),
                resolveSubMap(detail, "responseResult")
        );
    }

    @Override
    public PageResult<SystemLogVO> pageSystemLogs(long page, long pageSize, String level) {
        long safePage = normalizePage(page);
        long safePageSize = normalizePageSize(pageSize);
        String safeLevel = StringUtils.hasText(level) ? level.toUpperCase(Locale.ROOT) : "ERROR";
        if (!List.of("INFO", "WARN", "ERROR").contains(safeLevel)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的level参数");
        }

        LambdaQueryWrapper<SystemLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemLogEntity::getLevel, safeLevel)
                .orderByDesc(SystemLogEntity::getCreateTime);
        Page<SystemLogEntity> result = systemLogMapper.selectPage(new Page<>(safePage, safePageSize), wrapper);
        List<SystemLogVO> records = result.getRecords().stream()
                .map(item -> new SystemLogVO(
                        item.getObjectId(),
                        item.getCreateTime(),
                        item.getLevel(),
                        StringUtils.hasText(item.getModule()) ? item.getModule() : "admin-server",
                        summarize(item.getMessage())
                ))
                .toList();
        return new PageResult<>(records, result.getTotal(), safePage, safePageSize);
    }

    @Override
    public PageResult<SecurityLogVO> pageSecurityLogs(long page, long pageSize) {
        long safePage = normalizePage(page);
        long safePageSize = normalizePageSize(pageSize);

        LambdaQueryWrapper<SecurityLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SecurityLogEntity::getCreateTime);
        Page<SecurityLogEntity> result = securityLogMapper.selectPage(new Page<>(safePage, safePageSize), wrapper);

        Map<String, String> userDisplayMap = loadUserDisplayMap(result.getRecords().stream()
                .map(SecurityLogEntity::getUserId)
                .filter(StringUtils::hasText)
                .toList());

        List<SecurityLogVO> records = result.getRecords().stream()
                .map(item -> new SecurityLogVO(
                        item.getObjectId(),
                        item.getEventType(),
                        resolveSecurityResult(item.getDetail()),
                        userDisplayMap.getOrDefault(item.getUserId(), item.getUserId()),
                        item.getIpAddress(),
                        item.getCreateTime()
                ))
                .toList();
        return new PageResult<>(records, result.getTotal(), safePage, safePageSize);
    }

    @Override
    public LogExportVO exportLogs(LogExportRequest request) {
        validateExportRequest(request);
        AuthUser operator = securityUtil.getCurrentUser();
        assertExportNotDebounced(operator.objectId(), request);

        LocalDateTime startTime = request.getFilters() == null ? null : request.getFilters().getStartTime();
        LocalDateTime endTime = request.getFilters() == null ? null : request.getFilters().getEndTime();
        String module = request.getFilters() == null ? null : request.getFilters().getModule();
        validateTimeRange(startTime, endTime);

        String type = request.getType().toUpperCase(Locale.ROOT);
        LocalDateTime safeEnd = endTime == null ? LocalDateTime.now() : endTime;
        LocalDateTime safeStart = startTime == null ? safeEnd.minusHours(24) : startTime;

        try {
            Path exportDir = Path.of(System.getProperty("java.io.tmpdir"), "admin-log-exports");
            Files.createDirectories(exportDir);
            String fileName = "log_" + type.toLowerCase(Locale.ROOT) + "_" + UUID.randomUUID().toString().replace("-", "") + ".csv";
            Path filePath = exportDir.resolve(fileName);
            String csv = switch (type) {
                case "OPERATION" -> exportOperationCsv(safeStart, safeEnd, module);
                case "SYSTEM" -> exportSystemCsv(safeStart, safeEnd, module);
                case "SECURITY" -> exportSecurityCsv(safeStart, safeEnd);
                default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的type参数");
            };
            Files.writeString(filePath, csv, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            LocalDateTime expireTime = LocalDateTime.now().plusHours(1);
            String fileId = exportFileRegistry.register(filePath, expireTime, type);
            String downloadUrl = "/api/v1/logs/download?fileId=" + fileId;
            log.info("event=log_export_submit userId={} type={} fileId={}", operator.objectId(), type, fileId);
            return new LogExportVO(downloadUrl, expireTime);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("event=log_export_fail type={}", type, ex);
            throw new BusinessException(LOG_EXPORT_FAILED_CODE, "日志导出失败");
        }
    }

    @Override
    public LogStatsVO getStats() {
        int operationCount = operationLogMapper.selectCount(null).intValue();
        int loginCount = systemLogMapper.selectCount(new LambdaQueryWrapper<SystemLogEntity>()
                .eq(SystemLogEntity::getLevel, "INFO")
                .like(SystemLogEntity::getMessage, "login")).intValue();
        int errorCount = systemLogMapper.selectCount(new LambdaQueryWrapper<SystemLogEntity>()
                .eq(SystemLogEntity::getLevel, "ERROR")).intValue();
        int securityCount = securityLogMapper.selectCount(null).intValue();
        return new LogStatsVO(operationCount, loginCount, errorCount, securityCount);
    }

    @Override
    public Path resolveExportFile(String fileId) {
        return exportFileRegistry.resolve(fileId);
    }

    private void assertExportNotDebounced(String userId, LogExportRequest request) {
        String debounceKey = userId + ":" + buildExportSignature(request);
        long now = System.currentTimeMillis();
        Long last = exportDebounceMap.put(debounceKey, now);
        if (last != null && now - last < EXPORT_DEBOUNCE_MS) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "导出请求过于频繁，请稍后重试");
        }
        exportDebounceMap.entrySet().removeIf(entry -> now - entry.getValue() > EXPORT_DEBOUNCE_MS * 10);
    }

    private String buildExportSignature(LogExportRequest request) {
        LogExportRequest.Filters filters = request.getFilters();
        return request.getType() + "|" + request.getFormat() + "|"
                + (filters == null ? "" : filters.getStartTime()) + "|"
                + (filters == null ? "" : filters.getEndTime()) + "|"
                + (filters == null ? "" : filters.getModule());
    }

    private String exportOperationCsv(LocalDateTime startTime, LocalDateTime endTime, String module) throws IOException {
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(OperationLogEntity::getCreateTime, startTime)
                .le(OperationLogEntity::getCreateTime, endTime)
                .orderByDesc(OperationLogEntity::getCreateTime);
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLogEntity::getModule, module);
        }
        List<OperationLogEntity> rows = operationLogMapper.selectList(wrapper);
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                .setHeader("objectId", "userId", "module", "action", "result", "ipAddress", "createTime")
                .build())) {
            for (OperationLogEntity row : rows) {
                Map<String, Object> detail = parseJsonObject(row.getDetail());
                printer.printRecord(row.getObjectId(), row.getUserId(), row.getModule(), row.getAction(),
                        resolveOperationResult(detail), row.getIpAddress(), row.getCreateTime());
            }
        }
        return sw.toString();
    }

    private String exportSystemCsv(LocalDateTime startTime, LocalDateTime endTime, String module) throws IOException {
        LambdaQueryWrapper<SystemLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SystemLogEntity::getCreateTime, startTime)
                .le(SystemLogEntity::getCreateTime, endTime)
                .orderByDesc(SystemLogEntity::getCreateTime);
        if (StringUtils.hasText(module)) {
            wrapper.eq(SystemLogEntity::getModule, module);
        }
        List<SystemLogEntity> rows = systemLogMapper.selectList(wrapper);
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                .setHeader("objectId", "level", "module", "message", "createTime")
                .build())) {
            for (SystemLogEntity row : rows) {
                printer.printRecord(row.getObjectId(), row.getLevel(), row.getModule(), summarize(row.getMessage()), row.getCreateTime());
            }
        }
        return sw.toString();
    }

    private String exportSecurityCsv(LocalDateTime startTime, LocalDateTime endTime) throws IOException {
        LambdaQueryWrapper<SecurityLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SecurityLogEntity::getCreateTime, startTime)
                .le(SecurityLogEntity::getCreateTime, endTime)
                .orderByDesc(SecurityLogEntity::getCreateTime);
        List<SecurityLogEntity> rows = securityLogMapper.selectList(wrapper);
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                .setHeader("objectId", "userId", "eventType", "eventResult", "ipAddress", "createTime")
                .build())) {
            for (SecurityLogEntity row : rows) {
                printer.printRecord(row.getObjectId(), row.getUserId(), row.getEventType(), resolveSecurityResult(row.getDetail()),
                        row.getIpAddress(), row.getCreateTime());
            }
        }
        return sw.toString();
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startTime不能大于endTime");
        }
    }

    private void validateExportRequest(LogExportRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求体不能为空");
        }
        String safeType = request.getType() == null ? "" : request.getType().toUpperCase(Locale.ROOT);
        String safeFormat = request.getFormat() == null ? "" : request.getFormat().toUpperCase(Locale.ROOT);
        if (!List.of("OPERATION", "SYSTEM", "SECURITY").contains(safeType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的type参数");
        }
        if (!"CSV".equals(safeFormat)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持CSV导出");
        }
    }

    private long normalizePage(long page) {
        return page <= 0 ? DEFAULT_PAGE : page;
    }

    private long normalizePageSize(long pageSize) {
        long safe = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        if (safe > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "pageSize超过限制，最大为100");
        }
        return safe;
    }

    private Map<String, String> loadUserDisplayMap(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getObjectId, userIds);
        return userMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(User::getObjectId, user -> {
                    if (StringUtils.hasText(user.getNickname())) {
                        return user.getNickname();
                    }
                    if (StringUtils.hasText(user.getUsername())) {
                        return user.getUsername();
                    }
                    return user.getObjectId();
                }, (a, b) -> a));
    }

    private Map<String, Object> parseJsonObject(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            log.warn("parse log detail json failed", ex);
            return Map.of();
        }
    }

    private Map<String, Object> resolveSubMap(Map<String, Object> detail, String key) {
        if (detail == null || detail.isEmpty()) {
            return Map.of();
        }
        Object raw = detail.get(key);
        if (raw instanceof Map<?, ?> map) {
            Map<String, Object> result = new HashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return Map.of();
    }

    private String resolveOperationResult(Map<String, Object> detail) {
        if (detail == null || detail.isEmpty()) {
            return "UNKNOWN";
        }
        Object result = detail.get("result");
        return result == null ? "UNKNOWN" : String.valueOf(result);
    }

    private String resolveSecurityResult(String detail) {
        if (!StringUtils.hasText(detail)) {
            return "UNKNOWN";
        }
        String upper = detail.toUpperCase(Locale.ROOT);
        if (upper.contains("SUCCESS")) {
            return "SUCCESS";
        }
        if (upper.contains("DENIED")) {
            return "DENIED";
        }
        if (upper.contains("FAIL")) {
            return "FAILED";
        }
        return "UNKNOWN";
    }

    private String summarize(String message) {
        if (!StringUtils.hasText(message)) {
            return "";
        }
        String normalized = message.replace('\n', ' ').trim();
        if (normalized.length() <= 120) {
            return normalized;
        }
        return normalized.substring(0, 120) + "...";
    }
}

package com.platform.admin.modules.backup.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PageResult;
import com.platform.admin.modules.backup.dto.CreateScheduleRequest;
import com.platform.admin.modules.backup.dto.ManualBackupRequest;
import com.platform.admin.modules.backup.dto.UpdateScheduleRequest;
import com.platform.admin.modules.backup.entity.BackupRecordEntity;
import com.platform.admin.modules.backup.entity.BackupScheduleEntity;
import com.platform.admin.modules.backup.enums.BackupStatus;
import com.platform.admin.modules.backup.enums.BackupType;
import com.platform.admin.modules.backup.enums.RestoreTaskStatus;
import com.platform.admin.modules.backup.mapper.BackupAssembler;
import com.platform.admin.modules.backup.mapper.BackupRecordMapper;
import com.platform.admin.modules.backup.mapper.BackupScheduleMapper;
import com.platform.admin.modules.backup.mapper.UserLookupMapper;
import com.platform.admin.modules.backup.support.BackupAsyncExecutor;
import com.platform.admin.modules.backup.support.BackupCronUtil;
import com.platform.admin.modules.backup.support.BackupRecordReconciler;
import com.platform.admin.modules.backup.support.BackupServiceSupport;
import com.platform.admin.modules.backup.support.BackupStorage;
import com.platform.admin.common.log.SecurityLogEventType;
import com.platform.admin.common.log.SecurityLogWriter;
import com.platform.admin.common.util.ClientIpUtils;
import com.platform.admin.modules.backup.support.RestoreTaskStore;
import com.platform.admin.modules.backup.vo.BackupRecordDetailVO;
import com.platform.admin.modules.backup.vo.BackupRecordVO;
import com.platform.admin.modules.backup.vo.BackupScheduleVO;
import com.platform.admin.modules.backup.vo.DeleteBackupVO;
import com.platform.admin.modules.backup.vo.DeleteScheduleVO;
import com.platform.admin.modules.backup.vo.ManualBackupVO;
import com.platform.admin.modules.backup.vo.RestoreTaskVO;
import com.platform.admin.modules.backup.vo.StorageInfoVO;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BackupServiceImpl implements BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;

    private final SecurityUtil securityUtil;
    private final BackupRecordMapper backupRecordMapper;
    private final BackupScheduleMapper backupScheduleMapper;
    private final UserLookupMapper userLookupMapper;
    private final BackupStorage backupStorage;
    private final RestoreTaskStore restoreTaskStore;
    private final SecurityLogWriter securityLogWriter;
    private final BackupAsyncExecutor backupAsyncExecutor;
    private final BackupServiceSupport backupServiceSupport;
    private final BackupRecordReconciler backupRecordReconciler;

    public BackupServiceImpl(
            SecurityUtil securityUtil,
            BackupRecordMapper backupRecordMapper,
            BackupScheduleMapper backupScheduleMapper,
            UserLookupMapper userLookupMapper,
            BackupStorage backupStorage,
            RestoreTaskStore restoreTaskStore,
            SecurityLogWriter securityLogWriter,
            BackupAsyncExecutor backupAsyncExecutor,
            BackupServiceSupport backupServiceSupport,
            BackupRecordReconciler backupRecordReconciler
    ) {
        this.securityUtil = securityUtil;
        this.backupRecordMapper = backupRecordMapper;
        this.backupScheduleMapper = backupScheduleMapper;
        this.userLookupMapper = userLookupMapper;
        this.backupStorage = backupStorage;
        this.restoreTaskStore = restoreTaskStore;
        this.securityLogWriter = securityLogWriter;
        this.backupAsyncExecutor = backupAsyncExecutor;
        this.backupServiceSupport = backupServiceSupport;
        this.backupRecordReconciler = backupRecordReconciler;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManualBackupVO triggerManualBackup(ManualBackupRequest request) {
        AuthUser operator = requireAdmin();
        return startBackup(request.getBackupType(), request.getDescription(), operator.objectId(), true);
    }

    @Override
    public List<BackupScheduleVO> listSchedules() {
        AuthUser operator = requireAdmin();
        LambdaQueryWrapper<BackupScheduleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BackupScheduleEntity::getCreateTime);
        List<BackupScheduleVO> schedules = backupScheduleMapper.selectList(wrapper).stream()
                .map(BackupAssembler::toScheduleVO)
                .toList();
        auditBackupQuery(operator, "/api/v1/backup/schedules", "GET", Map.of());
        return schedules;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BackupScheduleVO createSchedule(CreateScheduleRequest request) {
        AuthUser operator = requireAdmin();
        LocalDateTime now = BackupCronUtil.now();
        LocalDateTime nextExecution = BackupCronUtil.nextExecutionTime(request.getCronExpression(), now);

        String objectId = UUID.randomUUID().toString();
        BackupScheduleEntity entity = BackupScheduleEntity.builder()
                .objectId(objectId)
                .cronExpression(request.getCronExpression())
                .backupType(request.getBackupType())
                .enabled(request.getEnabled() == null || request.getEnabled())
                .description(request.getDescription())
                .nextExecutionTime(nextExecution)
                .createTime(now)
                .updateTime(now)
                .build();
        backupScheduleMapper.insert(entity);

        auditBackupWrite(operator, SecurityLogEventType.BACKUP_SCHEDULE_CREATE, "POST",
                "/api/v1/backup/schedules",
                Map.of("objectId", objectId, "cronExpression", request.getCronExpression(),
                        "backupType", request.getBackupType().name()));
        return BackupAssembler.toScheduleVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BackupScheduleVO updateSchedule(String objectId, UpdateScheduleRequest request) {
        AuthUser operator = requireAdmin();
        BackupScheduleEntity entity = requireSchedule(objectId);

        if (StringUtils.hasText(request.getCronExpression())) {
            BackupCronUtil.nextExecutionTime(request.getCronExpression(), BackupCronUtil.now());
            entity.setCronExpression(request.getCronExpression());
            entity.setNextExecutionTime(
                    BackupCronUtil.nextExecutionTime(request.getCronExpression(), BackupCronUtil.now())
            );
        }
        if (request.getBackupType() != null) {
            entity.setBackupType(request.getBackupType());
        }
        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        entity.setUpdateTime(BackupCronUtil.now());
        backupScheduleMapper.updateById(entity);

        auditBackupWrite(operator, SecurityLogEventType.BACKUP_SCHEDULE_UPDATE, "PUT",
                "/api/v1/backup/schedules/" + objectId,
                Map.of("objectId", objectId, "enabled", entity.getEnabled()));
        return BackupAssembler.toScheduleVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteScheduleVO deleteSchedule(String objectId) {
        AuthUser operator = requireAdmin();
        requireSchedule(objectId);
        backupScheduleMapper.deleteById(objectId);

        auditBackupWrite(operator, SecurityLogEventType.BACKUP_SCHEDULE_DELETE, "DELETE",
                "/api/v1/backup/schedules/" + objectId,
                Map.of("objectId", objectId));
        return new DeleteScheduleVO(objectId, true);
    }

    @Override
    public PageResult<BackupRecordVO> pageRecords(
            long page,
            long pageSize,
            BackupStatus status,
            BackupType backupType
    ) {
        AuthUser operator = requireAdmin();
        long safePage = page <= 0 ? DEFAULT_PAGE : page;
        long safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        LambdaQueryWrapper<BackupRecordEntity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BackupRecordEntity::getStatus, status);
        }
        if (backupType != null) {
            wrapper.eq(BackupRecordEntity::getBackupType, backupType);
        }
        wrapper.orderByDesc(BackupRecordEntity::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BackupRecordEntity> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(safePage, safePageSize);
        var result = backupRecordMapper.selectPage(mpPage, wrapper);

        List<BackupRecordVO> records = result.getRecords().stream()
                .map(entity -> BackupAssembler.toRecordVO(entity, resolveOperatorName(entity.getOperatorId())))
                .toList();
        auditBackupQuery(operator, "/api/v1/backup/records", "GET",
                Map.of("page", safePage, "pageSize", safePageSize));
        return new PageResult<>(records, result.getTotal(), safePage, safePageSize);
    }

    @Override
    public BackupRecordDetailVO getRecordDetail(String objectId, String baseUrl) {
        AuthUser operator = requireAdmin();
        BackupRecordEntity entity = requireRecord(objectId);
        String downloadUrl = baseUrl + "/api/v1/backup/records/" + objectId + "/download";
        auditBackupQuery(operator, "/api/v1/backup/records/" + objectId, "GET", Map.of("objectId", objectId));
        return BackupAssembler.toRecordDetailVO(
                entity,
                resolveOperatorName(entity.getOperatorId()),
                downloadUrl
        );
    }

    @Override
    public Path resolveDownloadFile(String objectId) {
        AuthUser operator = requireAdmin();
        BackupRecordEntity entity = requireRecord(objectId);
        if (entity.getStatus() != BackupStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.BACKUP_FILE_NOT_FOUND, "备份文件不存在");
        }
        Path file = backupStorage.resolveStoredFile(entity.getFilePath());
        if (file == null || !Files.exists(file)) {
            throw new BusinessException(ErrorCode.BACKUP_FILE_NOT_FOUND, "备份文件不存在");
        }
        auditBackupQuery(operator, "/api/v1/backup/records/" + objectId + "/download", "GET",
                Map.of("objectId", objectId));
        return file;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteBackupVO deleteRecord(String objectId) {
        AuthUser operator = requireAdmin();
        BackupRecordEntity entity = requireRecord(objectId);
        if (entity.getStatus() == BackupStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONFLICT, "备份任务进行中，无法删除");
        }

        Path file = backupStorage.resolveStoredFile(entity.getFilePath());
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ex) {
                log.warn("backup file delete failed objectId={} path={}", objectId, file, ex);
            }
        }
        backupRecordMapper.deleteById(objectId);

        auditBackupWrite(operator, SecurityLogEventType.BACKUP_RECORD_DELETE, "DELETE",
                "/api/v1/backup/records/" + objectId,
                Map.of("objectId", objectId, "fileSize", entity.getFileSize()));
        return new DeleteBackupVO(objectId, true);
    }

    @Override
    public RestoreTaskVO restoreFromBackup(String objectId) {
        AuthUser operator = requireAdmin();
        ensureNoActiveBackupOrRestore();

        BackupRecordEntity entity = requireRecord(objectId);
        if (entity.getStatus() != BackupStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.BACKUP_FILE_NOT_FOUND, "备份文件不存在");
        }
        Path file = backupStorage.resolveStoredFile(entity.getFilePath());
        if (file == null || !Files.exists(file)) {
            throw new BusinessException(ErrorCode.BACKUP_FILE_NOT_FOUND, "备份文件不存在");
        }

        String restoreTaskId = restoreTaskStore.createTask(objectId);
        restoreTaskStore.markRestoreActive(restoreTaskId);

        auditBackupWrite(operator, SecurityLogEventType.BACKUP_RESTORE, "POST",
                "/api/v1/backup/restore/" + objectId,
                Map.of("objectId", objectId, "restoreTaskId", restoreTaskId));

        backupAsyncExecutor.executeRestoreAsync(restoreTaskId, file);
        log.info("event=backup_restore_trigger recordId={} restoreTaskId={}", objectId, restoreTaskId);

        return RestoreTaskVO.builder()
                .restoreTaskId(restoreTaskId)
                .backupRecordId(objectId)
                .status(RestoreTaskStatus.PROCESSING)
                .build();
    }

    @Override
    public RestoreTaskVO getRestoreTask(String restoreTaskId) {
        AuthUser operator = requireAdmin();
        Map<String, Object> task = restoreTaskStore.getTask(restoreTaskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        auditBackupQuery(operator, "/api/v1/backup/restore/" + restoreTaskId, "GET",
                Map.of("restoreTaskId", restoreTaskId));
        return RestoreTaskVO.builder()
                .restoreTaskId(String.valueOf(task.get("restoreTaskId")))
                .backupRecordId(String.valueOf(task.get("backupRecordId")))
                .status(RestoreTaskStatus.valueOf(String.valueOf(task.get("status"))))
                .build();
    }

    @Override
    public StorageInfoVO getStorageInfo() {
        AuthUser operator = requireAdmin();
        long backupCount = backupRecordMapper.selectCount(
                new LambdaQueryWrapper<BackupRecordEntity>().eq(BackupRecordEntity::getStatus, BackupStatus.SUCCESS)
        );
        auditBackupQuery(operator, "/api/v1/backup/storage-info", "GET", Map.of("backupCount", backupCount));
        return backupServiceSupport.buildStorageInfo(backupCount);
    }

    /**
     * 供定时任务调用的备份入口（无 HTTP 安全上下文）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void triggerScheduledBackup(BackupScheduleEntity schedule) {
        ensureNoActiveBackupOrRestore();
        String description = StringUtils.hasText(schedule.getDescription())
                ? schedule.getDescription()
                : "定时备份任务 " + schedule.getObjectId();
        startBackup(schedule.getBackupType(), description, null, false);

        LocalDateTime now = BackupCronUtil.now();
        schedule.setLastExecutionTime(now);
        schedule.setNextExecutionTime(BackupCronUtil.nextExecutionTime(schedule.getCronExpression(), now));
        schedule.setUpdateTime(now);
        backupScheduleMapper.updateById(schedule);
    }

    private ManualBackupVO startBackup(
            BackupType requestedType,
            String description,
            String operatorId,
            boolean writeManualLog
    ) {
        ensureNoActiveBackupOrRestore();

        BackupType backupType = resolveBackupType(requestedType);
        String resolvedDescription = buildDescription(description, requestedType, backupType);

        String objectId = UUID.randomUUID().toString();
        LocalDateTime now = BackupCronUtil.now();
        String fileName = backupStorage.buildFileName(objectId, backupType.name(), now);
        Path targetFile = backupStorage.resolveFilePath(fileName);

        String persistedOperatorId = resolvePersistedOperatorId(operatorId);

        BackupRecordEntity record = BackupRecordEntity.builder()
                .objectId(objectId)
                .backupType(backupType)
                .filePath(targetFile.toString())
                .fileSize(0L)
                .status(BackupStatus.IN_PROGRESS)
                .description(resolvedDescription)
                .operatorId(persistedOperatorId)
                .createTime(now)
                .build();
        backupRecordMapper.insert(record);

        if (writeManualLog && persistedOperatorId != null) {
            auditBackupWriteByUserId(persistedOperatorId, SecurityLogEventType.BACKUP_MANUAL, "POST",
                    "/api/v1/backup/manual",
                    Map.of("objectId", objectId, "backupType", backupType.name()));
        }

        scheduleBackupAfterCommit(objectId, targetFile);
        log.info("event=backup_trigger objectId={} backupType={} scheduled={}", objectId, backupType, !writeManualLog);

        return ManualBackupVO.builder()
                .objectId(objectId)
                .backupType(backupType)
                .status(BackupStatus.IN_PROGRESS)
                .description(resolvedDescription)
                .createTime(now)
                .build();
    }

    /**
     * 事务提交后再启动异步备份，避免异步线程读不到未提交的 backup_record。
     */
    private void scheduleBackupAfterCommit(String objectId, Path targetFile) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    backupAsyncExecutor.executeBackupAsync(objectId, targetFile);
                }
            });
            return;
        }
        backupAsyncExecutor.executeBackupAsync(objectId, targetFile);
    }

    /**
     * operator_id 有外键约束；Mock JWT 的 userId 可能不在 user 表，此时置 null 避免插入失败。
     */
    private String resolvePersistedOperatorId(String operatorId) {
        if (!StringUtils.hasText(operatorId)) {
            return null;
        }
        if (userLookupMapper.selectUsernameById(operatorId) == null) {
            log.warn("operator not found in database, backup_record.operator_id will be null operatorId={}", operatorId);
            return null;
        }
        return operatorId;
    }

    private AuthUser requireAdmin() {
        securityUtil.requireAdminWritePermission();
        return securityUtil.getCurrentUser();
    }

    private void auditBackupWrite(AuthUser operator, String eventType, String httpMethod, String path, Map<String, Object> extra) {
        auditBackupWriteByUserId(operator.objectId(), eventType, httpMethod, path, extra);
    }

    private void auditBackupWriteByUserId(String userId, String eventType, String httpMethod, String path, Map<String, Object> extra) {
        securityLogWriter.writeSecurityAccess(userId, eventType, resolveClientIp(), path, httpMethod, "SUCCESS", extra);
    }

    private void auditBackupQuery(AuthUser operator, String path, String httpMethod, Map<String, Object> extra) {
        securityLogWriter.writeSecurityAccess(
                operator.objectId(),
                SecurityLogEventType.BACKUP_QUERY,
                resolveClientIp(),
                path,
                httpMethod,
                "SUCCESS",
                extra
        );
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return ClientIpUtils.resolve(request);
    }

    private void ensureNoActiveBackupOrRestore() {
        if (restoreTaskStore.hasProcessingTask()) {
            throw new BusinessException(ErrorCode.CONFLICT, "系统正在恢复中，请等待完成");
        }
        if (backupRecordReconciler.hasActiveInProgressBackup()) {
            throw new BusinessException(ErrorCode.CONFLICT, "已有备份任务进行中，请稍后重试");
        }
    }

    private BackupRecordEntity requireRecord(String objectId) {
        BackupRecordEntity entity = backupRecordMapper.selectById(objectId);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return entity;
    }

    private BackupScheduleEntity requireSchedule(String objectId) {
        BackupScheduleEntity entity = backupScheduleMapper.selectById(objectId);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return entity;
    }

    private String resolveOperatorName(String operatorId) {
        if (!StringUtils.hasText(operatorId)) {
            return null;
        }
        return userLookupMapper.selectUsernameById(operatorId);
    }

    private BackupType resolveBackupType(BackupType requestedType) {
        if (requestedType != BackupType.INCREMENTAL) {
            return BackupType.FULL;
        }
        Long successCount = backupRecordMapper.selectCount(
                new LambdaQueryWrapper<BackupRecordEntity>().eq(BackupRecordEntity::getStatus, BackupStatus.SUCCESS)
        );
        if (successCount == null || successCount == 0) {
            return BackupType.FULL;
        }
        return BackupType.INCREMENTAL;
    }

    private String buildDescription(String description, BackupType requestedType, BackupType resolvedType) {
        String base = StringUtils.hasText(description) ? description : "";
        if (requestedType == BackupType.INCREMENTAL && resolvedType == BackupType.FULL) {
            String note = "（系统备注：尚无成功备份，已自动降级为全量备份）";
            return base.isBlank() ? note : base + note;
        }
        return base.isBlank() ? null : base;
    }
}

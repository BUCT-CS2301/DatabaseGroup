package com.platform.admin.modules.backup.controller;

import com.platform.admin.common.PageResult;
import com.platform.admin.common.Result;
import com.platform.admin.modules.backup.dto.CreateScheduleRequest;
import com.platform.admin.modules.backup.dto.ManualBackupRequest;
import com.platform.admin.modules.backup.dto.UpdateScheduleRequest;
import com.platform.admin.modules.backup.enums.BackupStatus;
import com.platform.admin.modules.backup.enums.BackupType;
import com.platform.admin.modules.backup.service.BackupService;
import com.platform.admin.modules.backup.vo.BackupRecordDetailVO;
import com.platform.admin.modules.backup.vo.BackupRecordVO;
import com.platform.admin.modules.backup.vo.BackupScheduleVO;
import com.platform.admin.modules.backup.vo.DeleteBackupVO;
import com.platform.admin.modules.backup.vo.DeleteScheduleVO;
import com.platform.admin.modules.backup.vo.ManualBackupVO;
import com.platform.admin.modules.backup.vo.RestoreTaskVO;
import com.platform.admin.modules.backup.vo.StorageInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/manual")
    public Result<ManualBackupVO> triggerManualBackup(@RequestBody @Valid ManualBackupRequest request) {
        return Result.success(backupService.triggerManualBackup(request));
    }

    @GetMapping("/schedules")
    public Result<List<BackupScheduleVO>> listSchedules() {
        return Result.success(backupService.listSchedules());
    }

    @PostMapping("/schedules")
    public Result<BackupScheduleVO> createSchedule(@RequestBody @Valid CreateScheduleRequest request) {
        return Result.success(backupService.createSchedule(request));
    }

    @PutMapping("/schedules/{objectId}")
    public Result<BackupScheduleVO> updateSchedule(
            @PathVariable String objectId,
            @RequestBody @Valid UpdateScheduleRequest request
    ) {
        return Result.success(backupService.updateSchedule(objectId, request));
    }

    @DeleteMapping("/schedules/{objectId}")
    public Result<DeleteScheduleVO> deleteSchedule(@PathVariable String objectId) {
        return Result.success(backupService.deleteSchedule(objectId));
    }

    @GetMapping("/records")
    public Result<PageResult<BackupRecordVO>> pageRecords(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize最小为1") @Max(value = 100, message = "pageSize最大为100") long pageSize,
            @RequestParam(required = false) BackupStatus status,
            @RequestParam(required = false) BackupType backupType
    ) {
        return Result.success(backupService.pageRecords(page, pageSize, status, backupType));
    }

    @GetMapping("/records/{objectId}")
    public Result<BackupRecordDetailVO> getRecordDetail(
            @PathVariable String objectId,
            HttpServletRequest request
    ) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return Result.success(backupService.getRecordDetail(objectId, baseUrl));
    }

    @GetMapping("/records/{objectId}/download")
    public ResponseEntity<Resource> downloadRecord(@PathVariable String objectId) throws IOException {
        Path file = backupService.resolveDownloadFile(objectId);
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(file))
                .body(resource);
    }

    @DeleteMapping("/records/{objectId}")
    public Result<DeleteBackupVO> deleteRecord(@PathVariable String objectId) {
        return Result.success(backupService.deleteRecord(objectId));
    }

    @PostMapping("/restore/{objectId}")
    public Result<RestoreTaskVO> restoreFromBackup(@PathVariable String objectId) {
        return Result.success(backupService.restoreFromBackup(objectId));
    }

    @GetMapping("/restore/{restoreTaskId}")
    public Result<RestoreTaskVO> getRestoreTask(@PathVariable String restoreTaskId) {
        return Result.success(backupService.getRestoreTask(restoreTaskId));
    }

    @GetMapping("/storage-info")
    public Result<StorageInfoVO> getStorageInfo() {
        return Result.success(backupService.getStorageInfo());
    }
}

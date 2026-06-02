package com.platform.admin.modules.log.controller;

import com.platform.admin.common.PageResult;
import com.platform.admin.common.Result;
import com.platform.admin.modules.log.dto.LogExportRequest;
import com.platform.admin.modules.log.service.LogService;
import com.platform.admin.modules.log.vo.LogExportVO;
import com.platform.admin.modules.log.vo.LogStatsVO;
import com.platform.admin.modules.log.vo.OperationLogDetailVO;
import com.platform.admin.modules.log.vo.OperationLogVO;
import com.platform.admin.modules.log.vo.SecurityLogVO;
import com.platform.admin.modules.log.vo.SystemLogVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/operation")
    public Result<PageResult<OperationLogVO>> pageOperationLogs(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize最小为1") @Max(value = 100, message = "pageSize最大为100") long pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return Result.success(logService.pageOperationLogs(page, pageSize, userId, module, startTime, endTime));
    }

    @GetMapping("/operation/{objectId}")
    public Result<OperationLogDetailVO> getOperationLogDetail(@PathVariable String objectId) {
        return Result.success(logService.getOperationLogDetail(objectId));
    }

    @GetMapping("/system")
    public Result<PageResult<SystemLogVO>> pageSystemLogs(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize最小为1") @Max(value = 100, message = "pageSize最大为100") long pageSize,
            @RequestParam(required = false) String level
    ) {
        return Result.success(logService.pageSystemLogs(page, pageSize, level));
    }

    @GetMapping("/security")
    public Result<PageResult<SecurityLogVO>> pageSecurityLogs(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize最小为1") @Max(value = 100, message = "pageSize最大为100") long pageSize
    ) {
        return Result.success(logService.pageSecurityLogs(page, pageSize));
    }

    @GetMapping("/stats")
    public Result<LogStatsVO> getStats() {
        return Result.success(logService.getStats());
    }

    @PostMapping("/export")
    public Result<LogExportVO> exportLogs(@RequestBody @Valid LogExportRequest request) {
        return Result.success(logService.exportLogs(request));
    }

    /**
     * 下载导出的日志 CSV 文件。
     *
     * @param fileId 导出文件标识
     * @return CSV 文件流
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadExport(
            @RequestParam @NotBlank(message = "fileId不能为空") String fileId
    ) throws IOException {
        Path file = logService.resolveExportFile(fileId);
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(Files.size(file))
                .body(resource);
    }
}

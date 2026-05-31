package com.platform.admin.modules.log.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogExportRequest {

    @NotBlank(message = "type不能为空")
    private String type;

    @NotBlank(message = "format不能为空")
    private String format;

    @Valid
    private Filters filters;

    @Data
    public static class Filters {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String module;
    }
}

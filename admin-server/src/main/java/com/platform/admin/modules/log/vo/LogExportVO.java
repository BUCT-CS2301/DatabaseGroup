package com.platform.admin.modules.log.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogExportVO {
    private String downloadUrl;
    private LocalDateTime expireTime;
}

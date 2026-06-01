package com.platform.admin.modules.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrowseHistoryCreateRequest {
    @NotBlank(message = "artifactId不能为空")
    private String artifactId;
}

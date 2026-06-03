package com.platform.admin.modules.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FavoriteCreateRequest {

    @NotBlank(message = "artifactId不能为空")
    private String artifactId;

    @Size(max = 100, message = "收藏夹名称不能超过100个字符")
    private String groupName;
}
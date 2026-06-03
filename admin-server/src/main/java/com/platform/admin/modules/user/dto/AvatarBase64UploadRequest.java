package com.platform.admin.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarBase64UploadRequest {

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    private String contentType;

    @NotBlank(message = "base64内容不能为空")
    private String base64;
}
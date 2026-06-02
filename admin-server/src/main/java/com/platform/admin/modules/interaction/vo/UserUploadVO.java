package com.platform.admin.modules.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserUploadVO {

    private String uploadId;

    private String artifactId;

    private String title;

    private String imageUrl;

    private String period;

    private String museum;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime reviewTime;

    private String reviewComment;
}
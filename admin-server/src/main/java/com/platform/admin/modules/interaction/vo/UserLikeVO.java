package com.platform.admin.modules.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserLikeVO {
    private String artifactId;
    private String artifactTitle;
    private String imageUrl;
    private LocalDateTime createTime;
}

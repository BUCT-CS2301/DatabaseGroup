package com.platform.admin.modules.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCommentVO {
    private String objectId;
    private String artifactId;
    private String artifactTitle;
    private String content;
    private String status;
    private Long likeCount;
    private LocalDateTime createTime;
}

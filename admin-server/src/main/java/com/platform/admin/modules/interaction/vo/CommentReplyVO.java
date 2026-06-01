package com.platform.admin.modules.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentReplyVO {
    private String objectId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String content;
    private Long likeCount;
    private LocalDateTime createTime;
}

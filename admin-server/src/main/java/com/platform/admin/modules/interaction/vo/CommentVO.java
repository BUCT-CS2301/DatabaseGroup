package com.platform.admin.modules.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentVO {
    private String objectId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String content;
    private Long likeCount;
    private Long replyCount;
    private LocalDateTime createTime;
    private List<CommentReplyVO> replies = new ArrayList<>();
}

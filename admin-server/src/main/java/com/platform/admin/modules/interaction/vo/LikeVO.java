package com.platform.admin.modules.interaction.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeVO {
    private long likeCount;
    private Boolean isLiked;
}

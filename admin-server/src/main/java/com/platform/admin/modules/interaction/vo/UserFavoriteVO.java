package com.platform.admin.modules.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFavoriteVO {
    private String artifactId;
    private String artifactTitle;
    private String groupName;
    private LocalDateTime createTime;
}

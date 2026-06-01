package com.platform.admin.modules.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBrowseHistoryVO {
    private String objectId;
    private String artifactId;
    private String artifactTitle;
    private String imageUrl;
    private LocalDateTime browseTime;
}

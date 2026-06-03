package com.platform.admin.modules.interaction.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FavoriteActionVO {
    private String artifactId;
    private String groupName;
    private Boolean isFavorited;
}
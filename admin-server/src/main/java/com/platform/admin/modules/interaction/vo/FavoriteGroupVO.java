package com.platform.admin.modules.interaction.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteGroupVO {
    private String groupName;
    private LocalDateTime createTime;
}
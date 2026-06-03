package com.platform.admin.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_privacy_setting")
public class UserPrivacySettingEntity {

    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("user_id")
    private String userId;

    @TableField("favorites_visible")
    private Integer favoritesVisible;

    @TableField("likes_visible")
    private Integer likesVisible;

    @TableField("comments_visible")
    private Integer commentsVisible;

    @TableField("uploads_visible")
    private Integer uploadsVisible;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
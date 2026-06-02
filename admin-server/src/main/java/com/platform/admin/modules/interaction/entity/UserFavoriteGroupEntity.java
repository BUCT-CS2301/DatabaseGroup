package com.platform.admin.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_favorite_group")
public class UserFavoriteGroupEntity {

    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("user_id")
    private String userId;

    @TableField("group_name")
    private String groupName;

    @TableField("create_time")
    private LocalDateTime createTime;
}
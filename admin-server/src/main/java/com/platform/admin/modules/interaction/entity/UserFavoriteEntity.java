package com.platform.admin.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_favorite")
public class UserFavoriteEntity {
    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("user_id")
    private String userId;

    @TableField("artifact_id")
    private String artifactId;

    @TableField("group_name")
    private String groupName;

    @TableField("create_time")
    private LocalDateTime createTime;
}

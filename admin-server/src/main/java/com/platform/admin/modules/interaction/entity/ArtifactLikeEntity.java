package com.platform.admin.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("artifact_like")
public class ArtifactLikeEntity {
    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("artifact_id")
    private String artifactId;

    @TableField("user_id")
    private String userId;

    @TableField("create_time")
    private LocalDateTime createTime;
}

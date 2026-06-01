package com.platform.admin.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ugc_comment")
public class UgcCommentEntity {
    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("artifact_id")
    private String artifactId;

    @TableField("user_id")
    private String userId;

    @TableField("parent_id")
    private String parentId;

    @TableField("content_text")
    private String contentText;

    private String status;
    private Integer likes;

    @TableField("create_time")
    private LocalDateTime createTime;
}

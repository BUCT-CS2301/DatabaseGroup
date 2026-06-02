package com.platform.admin.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_artifact_upload")
public class UserArtifactUploadEntity {

    @TableId("object_id")
    private String objectId;

    @TableField("user_id")
    private String userId;

    @TableField("artifact_id")
    private String artifactId;

    @TableField("image_path")
    private String imagePath;

    @TableField("status")
    private String status;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("review_comment")
    private String reviewComment;

    @TableField("create_time")
    private LocalDateTime createTime;
}
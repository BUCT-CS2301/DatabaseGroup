package com.platform.admin.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment_like")
public class CommentLikeEntity {
    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("comment_id")
    private String commentId;

    @TableField("user_id")
    private String userId;

    @TableField("create_time")
    private LocalDateTime createTime;
}

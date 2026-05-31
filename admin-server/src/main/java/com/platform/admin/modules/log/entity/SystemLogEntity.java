package com.platform.admin.modules.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_log")
public class SystemLogEntity {

    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    private String level;

    private String module;

    private String message;

    private String exception;

    @TableField("create_time")
    private LocalDateTime createTime;
}

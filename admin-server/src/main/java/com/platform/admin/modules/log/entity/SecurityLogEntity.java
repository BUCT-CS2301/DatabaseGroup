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
@TableName("security_log")
public class SecurityLogEntity {

    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("user_id")
    private String userId;

    @TableField("event_type")
    private String eventType;

    private String detail;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("create_time")
    private LocalDateTime createTime;
}

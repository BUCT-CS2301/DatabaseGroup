package com.platform.admin.modules.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
public class Role {
    @TableId(type = IdType.ASSIGN_UUID)
    private String objectId;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer isSystem;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
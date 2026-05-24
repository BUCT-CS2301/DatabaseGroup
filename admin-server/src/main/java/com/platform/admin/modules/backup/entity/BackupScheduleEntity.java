package com.platform.admin.modules.backup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.admin.modules.backup.enums.BackupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("backup_schedule")
public class BackupScheduleEntity {

    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("backup_type")
    private BackupType backupType;

    private Boolean enabled;

    private String description;

    @TableField("last_execution_time")
    private LocalDateTime lastExecutionTime;

    @TableField("next_execution_time")
    private LocalDateTime nextExecutionTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}

package com.platform.admin.modules.backup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.admin.modules.backup.enums.BackupStatus;
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
@TableName("backup_record")
public class BackupRecordEntity {

    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    @TableField("backup_type")
    private BackupType backupType;

    @TableField("file_path")
    private String filePath;

    @TableField("file_size")
    private Long fileSize;

    private BackupStatus status;

    private String description;

    @TableField("operator_id")
    private String operatorId;

    @TableField("create_time")
    private LocalDateTime createTime;
}

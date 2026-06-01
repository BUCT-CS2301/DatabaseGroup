package com.platform.admin.modules.artifact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文物图片索引表 {@code artifact_image}。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("artifact_image")
public class ArtifactImageEntity {

    @TableId(value = "file_name", type = IdType.INPUT)
    private String fileName;

    @TableField("artifact_id")
    private String artifactId;
}

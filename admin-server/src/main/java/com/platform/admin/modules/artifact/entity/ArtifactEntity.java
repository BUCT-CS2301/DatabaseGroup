package com.platform.admin.modules.artifact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("artifact")
public class ArtifactEntity {
    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;
    private String title;
    private String period;
    private String type;
    private String material;
    private String description;
    private String dimensions;

    @TableField("museum_id")
    private String museumId;

    @TableField("detail_url")
    private String detailUrl;

    @TableField("credit_line")
    private String creditLine;

    @TableField("accession_number")
    private String accessionNumber;

    @TableField("crawl_date")
    private LocalDate crawlDate;
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}

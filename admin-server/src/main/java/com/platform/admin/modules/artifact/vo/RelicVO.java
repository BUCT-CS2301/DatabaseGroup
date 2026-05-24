package com.platform.admin.modules.artifact.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RelicVO {
    private String objectId;
    private String title;
    private String period;
    private String type;
    private String material;
    private String description;
    private String dimensions;
    private String museumId;
    private String detailUrl;
    private String imageUrl;
    private String imagePath;
    private String creditLine;
    private String accessionNumber;
    private LocalDate crawlDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

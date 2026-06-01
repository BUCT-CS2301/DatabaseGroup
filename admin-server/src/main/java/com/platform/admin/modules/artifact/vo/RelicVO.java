package com.platform.admin.modules.artifact.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    /** 封面图：{@code artifact_image} 中按 file_name 升序首张的公网 URL */
    private String imageUrl;
    /** 详情多图轮播；列表接口可不返回 */
    private List<String> imageUrls;
    /** 浏览热度，列表与相关推荐使用 */
    private Integer popularity;
    private String creditLine;
    private String accessionNumber;
    private LocalDate crawlDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

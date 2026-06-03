package com.platform.admin.modules.artifact.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ArtifactDetailVO {
    private String objectId;
    private String title;
    private String period;
    private String type;
    private String material;
    private String description;
    private String dimensions;
    private String museum;
    private String location;
    private String detailUrl;
    private String imageUrl;
    private List<String> imageUrls;
    private String creditLine;
    private String accessionNumber;
    private Integer popularity;
}

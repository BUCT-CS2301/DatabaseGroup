package com.platform.admin.modules.artifact.vo;

import lombok.Data;

@Data
public class ArtifactSearchItemVO {
    private String objectId;
    private String title;
    private String period;
    private String type;
    private String material;
    private String museum;
    private String imageUrl;
    private Integer popularity;
}
package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ArtifactSearchResultVO {
    private long total;
    private long page;
    private long size;
    private List<ArtifactSearchItemVO> items;
}
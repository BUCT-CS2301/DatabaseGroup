package com.platform.admin.modules.artifact.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ArtifactPageVO {
    private List<ArtifactListItemVO> items;
    private long total;
    private long page;
    private long size;
}

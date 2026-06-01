package com.platform.admin.modules.artifact.mapper;

import lombok.Data;

/**
 * {@link ArtifactMapper#selectMuseumsWithArtifacts(int)} 查询行映射。
 */
@Data
public class RelicFilterMuseumRow {
    private String objectId;
    private String name;
    private String nameCn;
}

package com.platform.admin.modules.artifact.mapper;

import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.vo.RelicVO;

public final class ArtifactMapper {
    private ArtifactMapper() {
    }

    public static RelicVO toVO(ArtifactEntity entity) {
        return RelicVO.builder()
                .objectId(entity.getObjectId())
                .title(entity.getTitle())
                .period(entity.getPeriod())
                .type(entity.getType())
                .material(entity.getMaterial())
                .description(entity.getDescription())
                .dimensions(entity.getDimensions())
                .museumId(entity.getMuseumId())
                .detailUrl(entity.getDetailUrl())
                .imageUrl(entity.getImageUrl())
                .imagePath(entity.getImagePath())
                .creditLine(entity.getCreditLine())
                .accessionNumber(entity.getAccessionNumber())
                .crawlDate(entity.getCrawlDate())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .isDeleted(entity.getIsDeleted())
                .build();
    }
}

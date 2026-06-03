package com.platform.admin.modules.artifact.mapper;

import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.vo.RelicVO;

/**
 * 文物 Entity 与对外 VO 的转换（非 MyBatis Mapper）。
 */
public final class RelicAssembler {
    private RelicAssembler() {
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
                .creditLine(entity.getCreditLine())
                .accessionNumber(entity.getAccessionNumber())
                .crawlDate(entity.getCrawlDate())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .isDeleted(entity.getIsDeleted())
                .build();
    }
}

package com.platform.admin.modules.artifact.mapper;

import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.support.RelicPopularitySupport;
import com.platform.admin.modules.artifact.vo.RelicVO;

import java.util.List;

/**
 * 文物 Entity 与对外 VO 的转换（非 MyBatis Mapper）。
 */
public final class RelicAssembler {
    private RelicAssembler() {
    }

    public static RelicVO toVO(ArtifactEntity entity) {
        return toVO(entity, null, null);
    }

  /**
   * 转为对外 VO，可选填充多图与主图（详情接口）。
   *
   * @param entity    库表实体
   * @param imageUrls 多图 URL；为 null 时不设置
   * @param primaryUrl 覆盖主图 URL；为 null 时使用 entity.imageUrl
   */
  public static RelicVO toVO(ArtifactEntity entity, List<String> imageUrls, String primaryUrl) {
        String imageUrl = primaryUrl != null ? primaryUrl : entity.getImageUrl();
        RelicVO.RelicVOBuilder builder = RelicVO.builder()
                .objectId(entity.getObjectId())
                .title(entity.getTitle())
                .period(entity.getPeriod())
                .type(entity.getType())
                .material(entity.getMaterial())
                .description(entity.getDescription())
                .dimensions(entity.getDimensions())
                .museumId(entity.getMuseumId())
                .detailUrl(entity.getDetailUrl())
                .imageUrl(imageUrl)
                .imagePath(entity.getImagePath())
                .creditLine(entity.getCreditLine())
                .accessionNumber(entity.getAccessionNumber())
                .crawlDate(entity.getCrawlDate())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .isDeleted(entity.getIsDeleted())
                .popularity(RelicPopularitySupport.compute(entity.getCreateTime()));
        if (imageUrls != null) {
            builder.imageUrls(imageUrls);
        }
        return builder.build();
    }

  /** 相关推荐等场景：仅填充卡片所需字段与热度。 */
  public static RelicVO toBrowseCard(ArtifactEntity entity, String imageUrl) {
    return RelicVO.builder()
        .objectId(entity.getObjectId())
        .title(entity.getTitle())
        .period(entity.getPeriod())
        .type(entity.getType())
        .imageUrl(imageUrl)
        .popularity(RelicPopularitySupport.compute(entity.getCreateTime()))
        .build();
  }
}

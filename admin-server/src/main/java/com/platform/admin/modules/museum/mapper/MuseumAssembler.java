package com.platform.admin.modules.museum.mapper;

import com.platform.admin.modules.museum.entity.MuseumEntity;
import com.platform.admin.modules.museum.vo.MuseumVO;

/**
 * 博物馆 Entity 与对外 VO 的转换。
 */
public final class MuseumAssembler {
    private MuseumAssembler() {
    }

    public static MuseumVO toVO(MuseumEntity entity) {
        return MuseumVO.builder()
                .objectId(entity.getObjectId())
                .name(entity.getName())
                .nameCn(entity.getNameCn())
                .location(entity.getLocation())
                .website(entity.getWebsite())
                .build();
    }
}

package com.platform.admin.modules.artifact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.vo.ArtifactSearchItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文物表 artifact 数据访问。
 */
@Mapper
public interface ArtifactMapper extends BaseMapper<ArtifactEntity> {

    @Select("""
            SELECT COUNT(1)
            FROM artifact a
            LEFT JOIN museum m ON a.museum_id = m.object_id
            WHERE a.is_deleted = 0
              AND (
                    LOWER(COALESCE(a.title, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(a.period, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(a.type, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(a.material, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(m.name, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(m.name_cn, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
              )
            """)
    long countSearchArtifacts(@Param("keyword") String keyword);

    @Select("""
            SELECT
                a.object_id AS objectId,
                a.title AS title,
                a.period AS period,
                a.type AS type,
                a.material AS material,
                COALESCE(NULLIF(m.name_cn, ''), m.name, '') AS museum,
                a.image_url AS imageUrl,
                0 AS popularity
            FROM artifact a
            LEFT JOIN museum m ON a.museum_id = m.object_id
            WHERE a.is_deleted = 0
              AND (
                    LOWER(COALESCE(a.title, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(a.period, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(a.type, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(a.material, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(m.name, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                 OR LOWER(COALESCE(m.name_cn, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
              )
            ORDER BY a.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<ArtifactSearchItemVO> searchArtifacts(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );
}
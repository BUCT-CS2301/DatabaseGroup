package com.platform.admin.modules.artifact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文物表 {@code artifact} 数据访问（MyBatis-Plus）。
 */
@Mapper
public interface ArtifactMapper extends BaseMapper<ArtifactEntity> {

  @Select("""
      SELECT DISTINCT period FROM artifact
      WHERE is_deleted = 0 AND period IS NOT NULL AND period <> ''
      ORDER BY period ASC LIMIT #{limit}
      """)
  List<String> selectDistinctPeriods(@Param("limit") int limit);

  @Select("""
      SELECT DISTINCT type FROM artifact
      WHERE is_deleted = 0 AND type IS NOT NULL AND type <> ''
      ORDER BY type ASC LIMIT #{limit}
      """)
  List<String> selectDistinctTypes(@Param("limit") int limit);

  @Select("""
      SELECT DISTINCT material FROM artifact
      WHERE is_deleted = 0 AND material IS NOT NULL AND material <> ''
      ORDER BY material ASC LIMIT #{limit}
      """)
  List<String> selectDistinctMaterials(@Param("limit") int limit);

  @Select("""
      SELECT m.object_id AS objectId, m.name AS name, m.name_cn AS nameCn
      FROM museum m
      INNER JOIN artifact a ON a.museum_id = m.object_id AND a.is_deleted = 0
      GROUP BY m.object_id, m.name, m.name_cn
      ORDER BY m.name ASC
      LIMIT #{limit}
      """)
  List<RelicFilterMuseumRow> selectMuseumsWithArtifacts(@Param("limit") int limit);
}

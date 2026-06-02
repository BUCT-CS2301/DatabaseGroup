package com.platform.admin.modules.artifact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文物表 {@code artifact} 数据访问（MyBatis-Plus）。
 */
@Mapper
public interface ArtifactMapper extends BaseMapper<ArtifactEntity> {
    @Select("""
            <script>
            SELECT a.*
            FROM artifact a
            LEFT JOIN museum m ON m.object_id = a.museum_id
            WHERE a.is_deleted = 0
            <if test="keyword != null and keyword != ''">
              AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                   OR a.accession_number LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="period != null and period != ''">
              AND a.period = #{period}
            </if>
            <if test="type != null and type != ''">
              AND a.type = #{type}
            </if>
            <if test="material != null and material != ''">
              AND a.material = #{material}
            </if>
            <if test="museum != null and museum != ''">
              AND (m.name = #{museum} OR m.name_cn = #{museum})
            </if>
            <choose>
              <when test="sort != null and sort.equals('hot')">
                ORDER BY (
                  (SELECT COUNT(*) FROM user_browse_history h WHERE h.artifact_id = a.object_id) * 0.6
                  + (SELECT COUNT(*) FROM user_favorite f WHERE f.artifact_id = a.object_id) * 0.4
                ) DESC, a.create_time DESC
              </when>
              <when test="sort != null and sort.equals('name')">
                ORDER BY a.title ASC, a.create_time DESC
              </when>
              <when test="sort != null and sort.equals('period')">
                ORDER BY a.period ASC, a.create_time DESC
              </when>
              <otherwise>
                ORDER BY a.create_time DESC
              </otherwise>
            </choose>
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ArtifactEntity> selectPublicPage(@Param("offset") long offset,
                                          @Param("size") long size,
                                          @Param("keyword") String keyword,
                                          @Param("period") String period,
                                          @Param("type") String type,
                                          @Param("material") String material,
                                          @Param("museum") String museum,
                                          @Param("sort") String sort);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM artifact a
            LEFT JOIN museum m ON m.object_id = a.museum_id
            WHERE a.is_deleted = 0
            <if test="keyword != null and keyword != ''">
              AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                   OR a.accession_number LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="period != null and period != ''">
              AND a.period = #{period}
            </if>
            <if test="type != null and type != ''">
              AND a.type = #{type}
            </if>
            <if test="material != null and material != ''">
              AND a.material = #{material}
            </if>
            <if test="museum != null and museum != ''">
              AND (m.name = #{museum} OR m.name_cn = #{museum})
            </if>
            </script>
            """)
    long countPublicPage(@Param("keyword") String keyword,
                         @Param("period") String period,
                         @Param("type") String type,
                         @Param("material") String material,
                         @Param("museum") String museum);

    @Select("""
            SELECT file_name
            FROM artifact_image
            WHERE artifact_id = #{artifactId}
            ORDER BY file_name ASC
            """)
    List<String> selectImageFileNamesByArtifactId(@Param("artifactId") String artifactId);

    @Select("SELECT COUNT(*) FROM user_browse_history WHERE artifact_id = #{artifactId}")
    int countViewsByArtifactId(@Param("artifactId") String artifactId);

    @Select("SELECT COUNT(*) FROM user_favorite WHERE artifact_id = #{artifactId}")
    int countFavoritesByArtifactId(@Param("artifactId") String artifactId);
}

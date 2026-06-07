package com.platform.admin.modules.artifact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.vo.ArtifactSearchItemVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 文物表 artifact 数据访问。
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

    @Insert("""
            INSERT INTO artifact_image(file_name, artifact_id)
            VALUES (#{fileName}, #{artifactId})
            ON DUPLICATE KEY UPDATE artifact_id = VALUES(artifact_id)
            """)
    int upsertArtifactImage(@Param("fileName") String fileName, @Param("artifactId") String artifactId);

    @Delete("""
            DELETE FROM artifact_image
            WHERE artifact_id = #{artifactId}
              AND file_name <> #{keepFileName}
            """)
    int deleteArtifactImagesExcept(@Param("artifactId") String artifactId, @Param("keepFileName") String keepFileName);

    @Select("UPDATE artifact SET update_time = #{updateTime} WHERE object_id = #{objectId}")
    int updateTimeById(@Param("objectId") String objectId, @Param("updateTime") java.time.LocalDateTime updateTime);

    @Select("""
            SELECT COUNT(1)
            FROM artifact a
            WHERE a.is_deleted = 0
              AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                   OR a.accession_number LIKE CONCAT('%', #{keyword}, '%'))
            """)
    long countSearchArtifacts(@Param("keyword") String keyword);

    @Select("""
            SELECT a.object_id AS objectId, a.title, a.period, a.type, a.material,
                   a.accession_number AS accessionNumber, m.name AS museum
            FROM artifact a
            LEFT JOIN museum m ON m.object_id = a.museum_id
            WHERE a.is_deleted = 0
              AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                   OR a.accession_number LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY a.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<ArtifactSearchItemVO> searchArtifacts(@Param("keyword") String keyword,
                                               @Param("offset") long offset,
                                               @Param("size") long size);
}

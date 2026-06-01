package com.platform.admin.modules.artifact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.artifact.entity.ArtifactImageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ArtifactImageMapper extends BaseMapper<ArtifactImageEntity> {

    @Select("""
            SELECT file_name, artifact_id FROM artifact_image
            WHERE artifact_id = #{artifactId}
            ORDER BY file_name ASC
            """)
    List<ArtifactImageEntity> selectByArtifactId(@Param("artifactId") String artifactId);

    @Select("""
            <script>
            SELECT file_name, artifact_id FROM artifact_image
            WHERE artifact_id IN
            <foreach collection="artifactIds" item="id" open="(" separator="," close=")">
              #{id}
            </foreach>
            ORDER BY artifact_id ASC, file_name ASC
            </script>
            """)
    List<ArtifactImageEntity> selectByArtifactIds(@Param("artifactIds") Collection<String> artifactIds);
}

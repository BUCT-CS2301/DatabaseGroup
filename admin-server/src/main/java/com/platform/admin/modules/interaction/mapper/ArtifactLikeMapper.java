package com.platform.admin.modules.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.interaction.entity.ArtifactLikeEntity;
import com.platform.admin.modules.interaction.vo.UserLikeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArtifactLikeMapper extends BaseMapper<ArtifactLikeEntity> {
    @Select("""
            SELECT l.artifact_id AS artifactId,
                   a.title AS artifactTitle,
                   (SELECT img.file_name FROM artifact_image img
                    WHERE img.artifact_id = l.artifact_id LIMIT 1) AS imageUrl,
                   l.create_time AS createTime
            FROM artifact_like l
            LEFT JOIN artifact a ON a.object_id = l.artifact_id
            WHERE l.user_id = #{userId}
            ORDER BY l.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<UserLikeVO> selectUserLikes(@Param("userId") String userId,
                                     @Param("offset") long offset,
                                     @Param("size") long size);
}

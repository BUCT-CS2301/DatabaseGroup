package com.platform.admin.modules.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.interaction.entity.UserArtifactUploadEntity;
import com.platform.admin.modules.interaction.vo.UserUploadVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserArtifactUploadMapper extends BaseMapper<UserArtifactUploadEntity> {

    @Select("""
            SELECT
                u.object_id AS uploadId,
                u.artifact_id AS artifactId,
                a.title AS title,
                u.image_path AS imageUrl,
                a.period AS period,
                COALESCE(m.name_cn, m.name, '') AS museum,
                u.status AS status,
                u.create_time AS createTime,
                u.review_time AS reviewTime,
                u.review_comment AS reviewComment
            FROM user_artifact_upload u
            LEFT JOIN artifact a ON a.object_id = u.artifact_id
            LEFT JOIN museum m ON m.object_id = a.museum_id
            WHERE u.user_id = #{userId}
            ORDER BY u.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<UserUploadVO> selectUserUploads(@Param("userId") String userId,
                                         @Param("offset") long offset,
                                         @Param("size") long size);

    @Select("""
            SELECT
                u.object_id AS uploadId,
                u.artifact_id AS artifactId,
                a.title AS title,
                u.image_path AS imageUrl,
                a.period AS period,
                COALESCE(m.name_cn, m.name, '') AS museum,
                u.status AS status,
                u.create_time AS createTime,
                u.review_time AS reviewTime,
                u.review_comment AS reviewComment
            FROM user_artifact_upload u
            LEFT JOIN artifact a ON a.object_id = u.artifact_id
            LEFT JOIN museum m ON m.object_id = a.museum_id
            WHERE u.user_id = #{userId}
              AND u.object_id = #{uploadId}
            LIMIT 1
            """)
    UserUploadVO selectUserUploadDetail(@Param("userId") String userId,
                                        @Param("uploadId") String uploadId);
}
package com.platform.admin.modules.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.interaction.entity.UgcCommentEntity;
import com.platform.admin.modules.interaction.vo.CommentReplyVO;
import com.platform.admin.modules.interaction.vo.CommentVO;
import com.platform.admin.modules.interaction.vo.UserCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UgcCommentMapper extends BaseMapper<UgcCommentEntity> {
    @Select("""
            SELECT c.object_id AS objectId,
                   c.user_id AS userId,
                   COALESCE(NULLIF(u.nickname, ''), u.username) AS userName,
                   u.avatar AS userAvatar,
                   c.content_text AS content,
                   c.likes AS likeCount,
                   (SELECT COUNT(1) FROM ugc_comment r
                    WHERE r.parent_id = c.object_id AND r.status = 'APPROVED') AS replyCount,
                   c.create_time AS createTime
            FROM ugc_comment c
            LEFT JOIN user u ON u.object_id = c.user_id
            WHERE c.artifact_id = #{artifactId}
              AND c.status = 'APPROVED'
              AND c.parent_id IS NULL
            ORDER BY c.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<CommentVO> selectTopLevelComments(@Param("artifactId") String artifactId,
                                           @Param("offset") long offset,
                                           @Param("size") long size);

    @Select("""
            SELECT COUNT(1)
            FROM ugc_comment
            WHERE artifact_id = #{artifactId}
              AND status = 'APPROVED'
              AND parent_id IS NULL
            """)
    long countTopLevelComments(@Param("artifactId") String artifactId);

    @Select("""
            SELECT r.object_id AS objectId,
                   r.user_id AS userId,
                   COALESCE(NULLIF(u.nickname, ''), u.username) AS userName,
                   u.avatar AS userAvatar,
                   r.content_text AS content,
                   r.likes AS likeCount,
                   r.create_time AS createTime
            FROM ugc_comment r
            LEFT JOIN user u ON u.object_id = r.user_id
            WHERE r.parent_id = #{parentId}
              AND r.status = 'APPROVED'
            ORDER BY r.create_time ASC
            LIMIT 5
            """)
    List<CommentReplyVO> selectReplies(@Param("parentId") String parentId);

    @Select("""
            SELECT c.object_id AS objectId,
                   c.artifact_id AS artifactId,
                   a.title AS artifactTitle,
                   c.content_text AS content,
                   c.status AS status,
                   c.likes AS likeCount,
                   c.create_time AS createTime
            FROM ugc_comment c
            LEFT JOIN artifact a ON a.object_id = c.artifact_id
            WHERE c.user_id = #{userId}
            ORDER BY c.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<UserCommentVO> selectUserComments(@Param("userId") String userId,
                                           @Param("offset") long offset,
                                           @Param("size") long size);
}

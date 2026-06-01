package com.platform.admin.modules.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.interaction.entity.UserFavoriteEntity;
import com.platform.admin.modules.interaction.vo.UserFavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavoriteEntity> {
    @Select("""
            SELECT f.artifact_id AS artifactId,
                   a.title AS artifactTitle,
                   f.group_name AS groupName,
                   f.create_time AS createTime
            FROM user_favorite f
            LEFT JOIN artifact a ON a.object_id = f.artifact_id
            WHERE f.user_id = #{userId}
            ORDER BY f.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<UserFavoriteVO> selectUserFavorites(@Param("userId") String userId,
                                             @Param("offset") long offset,
                                             @Param("size") long size);
}

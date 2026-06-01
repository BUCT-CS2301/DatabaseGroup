package com.platform.admin.modules.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.interaction.entity.UserBrowseHistoryEntity;
import com.platform.admin.modules.interaction.vo.UserBrowseHistoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistoryEntity> {
    @Select("""
            SELECT h.object_id AS objectId,
                   h.artifact_id AS artifactId,
                   a.title AS artifactTitle,
                   (SELECT img.file_name FROM artifact_image img
                    WHERE img.artifact_id = h.artifact_id LIMIT 1) AS imageUrl,
                   h.browse_time AS browseTime
            FROM user_browse_history h
            LEFT JOIN artifact a ON a.object_id = h.artifact_id
            WHERE h.user_id = #{userId}
            ORDER BY h.browse_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<UserBrowseHistoryVO> selectUserHistory(@Param("userId") String userId,
                                                @Param("offset") long offset,
                                                @Param("size") long size);
}

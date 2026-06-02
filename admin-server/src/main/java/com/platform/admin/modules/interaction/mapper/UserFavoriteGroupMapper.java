package com.platform.admin.modules.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.interaction.entity.UserFavoriteGroupEntity;
import com.platform.admin.modules.interaction.vo.FavoriteGroupSummaryVO;
import com.platform.admin.modules.interaction.vo.FavoriteGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFavoriteGroupMapper extends BaseMapper<UserFavoriteGroupEntity> {

    @Select("""
            SELECT
                g.group_name AS groupName,
                g.create_time AS createTime
            FROM user_favorite_group g
            WHERE g.user_id = #{userId}
            ORDER BY g.create_time ASC
            """)
    List<FavoriteGroupVO> selectGroups(@Param("userId") String userId);

    @Select("""
            SELECT
                g.group_name AS groupName,
                COUNT(f.object_id) AS count
            FROM user_favorite_group g
            LEFT JOIN user_favorite f
              ON f.user_id = g.user_id
             AND f.group_name = g.group_name
            WHERE g.user_id = #{userId}
            GROUP BY g.group_name
            ORDER BY g.create_time ASC
            """)
    List<FavoriteGroupSummaryVO> selectGroupSummary(@Param("userId") String userId);
}
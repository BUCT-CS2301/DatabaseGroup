package com.platform.admin.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.modules.user.entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("SELECT u.*, GROUP_CONCAT(r.role_code) as roles " +
            "FROM user u " +
            "LEFT JOIN user_role ur ON u.object_id = ur.user_id " +
            "LEFT JOIN role r ON ur.role_id = r.object_id " +
            "WHERE u.is_deleted = 0 " +
            "AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (u.status = #{status} OR #{status} = '') " +
            "GROUP BY u.object_id")
    IPage<UserEntity> selectUserPage(Page<UserEntity> page, 
                                     @Param("keyword") String keyword, 
                                     @Param("status") String status);
}
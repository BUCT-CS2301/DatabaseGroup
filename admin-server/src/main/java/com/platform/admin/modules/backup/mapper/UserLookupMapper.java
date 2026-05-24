package com.platform.admin.modules.backup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserLookupMapper {

    @Select("SELECT username FROM user WHERE object_id = #{objectId} LIMIT 1")
    String selectUsernameById(String objectId);
}

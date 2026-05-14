package com.platform.admin.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.modules.user.entity.UserEntity;
import com.platform.admin.modules.user.entity.UserActionLogEntity;

import java.util.List;
import java.util.Map;

public interface UserService {

    IPage<UserEntity> getUserPage(Page<UserEntity> page, String keyword, String status);

    UserEntity getUserById(String objectId);

    UserEntity createUser(UserEntity user, List<String> roleIds);

    UserEntity updateUser(String objectId, UserEntity user, List<String> roleIds);

    void deleteUser(String objectId);

    void updateUserStatus(String objectId, String status);

    IPage<UserActionLogEntity> getUserActionLogs(Page<UserActionLogEntity> page, String userId);
}
package com.platform.admin.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.BusinessException;
import com.platform.admin.modules.user.entity.UserActionLogEntity;
import com.platform.admin.modules.user.entity.UserEntity;
import com.platform.admin.modules.user.mapper.UserActionLogMapper;
import com.platform.admin.modules.user.mapper.UserMapper;
import com.platform.admin.modules.role.entity.RoleEntity;
import com.platform.admin.modules.role.mapper.RoleMapper;
import com.platform.admin.modules.role.entity.UserRoleEntity;
import com.platform.admin.modules.role.mapper.UserRoleMapper;
import com.platform.admin.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserActionLogMapper userActionLogMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public IPage<UserEntity> getUserPage(Page<UserEntity> page, String keyword, String status) {
        if (keyword == null) keyword = "";
        if (status == null) status = "";
        return userMapper.selectUserPage(page, keyword, status);
    }

    @Override
    public UserEntity getUserById(String objectId) {
        return userMapper.selectById(objectId);
    }

    @Override
    @Transactional
    public UserEntity createUser(UserEntity user, List<String> roleIds) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setStatus("ENABLED");
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(user.getObjectId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        return user;
    }

    @Override
    @Transactional
    public UserEntity updateUser(String objectId, UserEntity user, List<String> roleIds) {
        UserEntity existing = userMapper.selectById(objectId);
        if (existing == null || existing.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }

        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getAvatar() != null) existing.setAvatar(user.getAvatar());
        existing.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(existing);

        if (roleIds != null) {
            LambdaQueryWrapper<UserRoleEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRoleEntity::getUserId, objectId);
            userRoleMapper.delete(wrapper);

            for (String roleId : roleIds) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(objectId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        return existing;
    }

    @Override
    @Transactional
    public void deleteUser(String objectId) {
        UserEntity user = userMapper.selectById(objectId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setIsDeleted(1);
        user.setStatus("DISABLED");
        userMapper.updateById(user);
    }

    @Override
    public void updateUserStatus(String objectId, String status) {
        UserEntity user = userMapper.selectById(objectId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public IPage<UserActionLogEntity> getUserActionLogs(Page<UserActionLogEntity> page, String userId) {
        LambdaQueryWrapper<UserActionLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActionLogEntity::getUserId, userId);
        wrapper.orderByDesc(UserActionLogEntity::getCreateTime);
        return userActionLogMapper.selectPage(page, wrapper);
    }
}
package com.platform.admin.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import com.platform.admin.modules.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).eq("is_deleted", 0);
        return getOne(wrapper);
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).eq("is_deleted", 0);
        return getOne(wrapper);
    }

    @Override
    public User getUserByPhone(String phone) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone).eq("is_deleted", 0);
        return getOne(wrapper);
    }

    @Override
    public User getUserByEmail(String email) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email).eq("is_deleted", 0);
        return getOne(wrapper);
    }

    @Override
    public User login(String username, String password) {
        User user = getByUsername(username);
        if (user != null && "ENABLED".equals(user.getStatus()) && passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    @Override
    public User register(User user) {
        user.setObjectId(UUID.randomUUID().toString());
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setStatus("ENABLED");
        if (user.getUserType() == null || user.getUserType().isBlank()) {
            user.setUserType("ADMIN");
        }
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public void updateLoginInfo(Long userId, String ip) {
    }

    @Override
    public void updateLoginInfo(String objectId, String ip) {
        User user = getById(objectId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
        }
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        return false;
    }

    @Override
    public boolean changePassword(String objectId, String oldPassword, String newPassword) {
        User user = getById(objectId);
        if (user != null && passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
            return true;
        }
        return false;
    }
}

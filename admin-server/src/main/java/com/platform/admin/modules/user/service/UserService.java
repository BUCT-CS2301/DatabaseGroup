package com.platform.admin.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.admin.modules.user.entity.User;

public interface UserService extends IService<User> {
    User getByUsername(String username);
    User getUserByUsername(String username);
    User getUserByPhone(String phone);
    User getUserByEmail(String email);
    User login(String username, String password);
    User register(User user);
    void updateLoginInfo(Long userId, String ip);
    void updateLoginInfo(String objectId, String ip);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    boolean changePassword(String objectId, String oldPassword, String newPassword);
}
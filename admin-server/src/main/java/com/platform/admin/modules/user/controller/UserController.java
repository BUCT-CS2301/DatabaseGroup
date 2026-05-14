package com.platform.admin.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.PageResult;
import com.platform.admin.common.Result;
import com.platform.admin.modules.user.entity.UserActionLogEntity;
import com.platform.admin.modules.user.entity.UserEntity;
import com.platform.admin.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<PageResult<UserEntity>> getUserList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        Page<UserEntity> pageParam = new Page<>(page, pageSize);
        IPage<UserEntity> userPage = userService.getUserPage(pageParam, keyword, status);
        
        PageResult<UserEntity> result = new PageResult<>(
                userPage.getRecords(),
                userPage.getTotal(),
                userPage.getCurrent(),
                userPage.getSize()
        );
        return Result.success(result);
    }

    @GetMapping("/{objectId}")
    public Result<UserEntity> getUserDetail(@PathVariable String objectId) {
        UserEntity user = userService.getUserById(objectId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<UserEntity> createUser(@RequestBody Map<String, Object> body) {
        UserEntity user = new UserEntity();
        user.setUsername((String) body.get("username"));
        user.setPasswordHash((String) body.get("password"));
        user.setNickname((String) body.get("nickname"));
        user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone"));
        
        @SuppressWarnings("unchecked")
        List<String> roleIds = (List<String>) body.get("roleIds");
        
        UserEntity created = userService.createUser(user, roleIds);
        return Result.success(created);
    }

    @PutMapping("/{objectId}")
    public Result<UserEntity> updateUser(@PathVariable String objectId, @RequestBody Map<String, Object> body) {
        UserEntity user = new UserEntity();
        if (body.containsKey("nickname")) user.setNickname((String) body.get("nickname"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        
        @SuppressWarnings("unchecked")
        List<String> roleIds = (List<String>) body.get("roleIds");
        
        UserEntity updated = userService.updateUser(objectId, user, roleIds);
        return Result.success(updated);
    }

    @DeleteMapping("/{objectId}")
    public Result<Void> deleteUser(@PathVariable String objectId) {
        userService.deleteUser(objectId);
        return Result.success(null);
    }

    @PutMapping("/{objectId}/status")
    public Result<Void> updateUserStatus(@PathVariable String objectId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        userService.updateUserStatus(objectId, status);
        return Result.success(null);
    }

    @GetMapping("/{objectId}/logs")
    public Result<PageResult<UserActionLogEntity>> getUserLogs(
            @PathVariable String objectId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        
        Page<UserActionLogEntity> pageParam = new Page<>(page, pageSize);
        IPage<UserActionLogEntity> logPage = userService.getUserActionLogs(pageParam, objectId);
        
        PageResult<UserActionLogEntity> result = new PageResult<>(
                logPage.getRecords(),
                logPage.getTotal(),
                logPage.getCurrent(),
                logPage.getSize()
        );
        return Result.success(result);
    }
}
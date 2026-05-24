package com.platform.admin.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<User> pageRequest = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select(User.class, info -> !info.getColumn().equals("password_hash"));
        wrapper.eq("is_deleted", 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("username", keyword).or().like("nickname", keyword).or().like("email", keyword);
        }
        wrapper.orderByDesc("create_time");
        IPage<User> result = userService.page(pageRequest, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable String id) {
        User user = userService.getById(id);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @PostMapping
    public Result<User> create(@RequestBody User user) {
        User existing = userService.getByUsername(user.getUsername());
        if (existing != null) {
            return Result.error(ErrorCode.CONFLICT, "用户名已存在");
        }
        User created = userService.register(user);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable String id, @RequestBody User user) {
        User existing = userService.getById(id);
        if (existing == null || existing.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!existing.getUsername().equals(user.getUsername())) {
            User check = userService.getByUsername(user.getUsername());
            if (check != null && !check.getObjectId().equals(id)) {
                return Result.error(ErrorCode.CONFLICT, "用户名已存在");
            }
        }
        user.setObjectId(id);
        user.setPasswordHash(existing.getPasswordHash());
        user.setIsDeleted(0);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        User user = userService.getById(id);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setIsDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        return Result.success(null);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String username = request.get("username");
        String password = request.get("password");
        User user = userService.login(username, password);
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        String ip = getClientIp(httpRequest);
        userService.updateLoginInfo(user.getObjectId(), ip);
        user.setPasswordHash(null);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        return Result.success(result);
    }

    @PostMapping("/{id}/change-password")
    public Result<Void> changePassword(@PathVariable String id, @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        boolean success = userService.changePassword(id, oldPassword, newPassword);
        if (!success) {
            return Result.error(ErrorCode.BAD_REQUEST, "原密码不正确");
        }
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result<User> updateStatus(@PathVariable String id, @RequestBody Map<String, String> request) {
        User user = userService.getById(id);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(request.get("status"));
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        user.setPasswordHash(null);
        return Result.success(user);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
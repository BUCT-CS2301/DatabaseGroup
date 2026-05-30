package com.platform.admin.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role) {
        Page<User> pageRequest = new Page<>(page, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select(User.class, info -> !info.getColumn().equals("password_hash"));
        wrapper.eq("is_deleted", 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("username", keyword).or().like("nickname", keyword).or().like("email", keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq("user_type", role);
        }
        wrapper.orderByDesc("create_time");
        IPage<User> result = userService.page(pageRequest, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords().stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("objectId", user.getObjectId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("status", user.getStatus());
            userMap.put("roles", new String[]{user.getUserType()});
            userMap.put("lastLoginTime", user.getLastLoginTime());
            userMap.put("createTime", user.getCreateTime());
            return userMap;
        }).toList());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        
        return Result.success(data);
    }

    @GetMapping("/{objectId}")
    public Result<Map<String, Object>> getById(@PathVariable String objectId) {
        User user = userService.getById(objectId);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", user.getObjectId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("status", user.getStatus());
        data.put("roles", new String[]{user.getUserType()});
        data.put("lastLoginTime", user.getLastLoginTime());
        data.put("createTime", user.getCreateTime());
        
        return Result.success(data);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String nickname = (String) request.get("nickname");
        String email = (String) request.get("email");
        String phone = (String) request.get("phone");
        String role = getRequestRole(request, "USER");
        
        User existing = userService.getByUsername(username);
        if (existing != null) {
            return Result.error(ErrorCode.CONFLICT, "用户名已存在");
        }
        
        User user = new User();
        user.setObjectId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPasswordHash(password);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setUserType(role);
        user.setStatus("ENABLED");
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userService.register(user);
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", user.getObjectId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("status", user.getStatus());
        data.put("roles", new String[]{user.getUserType()});
        data.put("createTime", user.getCreateTime());
        
        return Result.success(data);
    }

    @PutMapping("/{objectId}")
    public Result<Map<String, Object>> update(@PathVariable String objectId, @RequestBody Map<String, Object> request) {   
        User existing = userService.getById(objectId);
        if (existing == null || existing.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        String nickname = (String) request.get("nickname");
        String email = (String) request.get("email");
        String phone = (String) request.get("phone");
        String role = getRequestRole(request, existing.getUserType());
        
        existing.setNickname(nickname);
        existing.setEmail(email);
        existing.setPhone(phone);
        existing.setUserType(role);
        existing.setUpdateTime(LocalDateTime.now());
        
        userService.updateById(existing);
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", existing.getObjectId());
        data.put("username", existing.getUsername());
        data.put("nickname", existing.getNickname());
        data.put("email", existing.getEmail());
        data.put("phone", existing.getPhone());
        data.put("status", existing.getStatus());
        data.put("roles", new String[]{existing.getUserType()});
        data.put("updateTime", existing.getUpdateTime());
        
        return Result.success(data);
    }

    @PutMapping("/{objectId}/role")
    public Result<Map<String, Object>> updateRole(@PathVariable String objectId, @RequestBody Map<String, Object> request) {
        User user = userService.getById(objectId);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }

        String role = getRequestRole(request, user.getUserType());
        user.setUserType(role);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);

        Map<String, Object> data = new HashMap<>();
        data.put("objectId", user.getObjectId());
        data.put("roles", new String[]{user.getUserType()});
        return Result.success(data);
    }

    @PutMapping("/{objectId}/password")
    public Result<Void> resetPassword(@PathVariable String objectId, @RequestBody Map<String, String> request) {
        User user = userService.getById(objectId);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }

        String password = request.get("password");
        if (password == null || password.length() < 6) {
            return Result.error(ErrorCode.BAD_REQUEST, "密码至少 6 位");
        }

        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        return Result.success(null);
    }

    @DeleteMapping("/{objectId}")
    public Result<Void> delete(@PathVariable String objectId) {
        User user = userService.getById(objectId);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setIsDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        return Result.success(null);
    }

    @PutMapping("/{objectId}/status")
    public Result<Map<String, Object>> updateStatus(@PathVariable String objectId, @RequestBody Map<String, String> request) {
        User user = userService.getById(objectId);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        String status = request.get("status");
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", user.getObjectId());
        data.put("status", user.getStatus());
        
        return Result.success(data);
    }

    @GetMapping("/{objectId}/logs")
    public Result<Map<String, Object>> getUserLogs(
            @PathVariable String objectId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("records", new Object[]{});
        data.put("total", 0);
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @SuppressWarnings("unchecked")
    private String getRequestRole(Map<String, Object> request, String defaultRole) {
        Object roleValue = request.get("role");
        if (roleValue instanceof String role && !role.isBlank()) {
            return role;
        }
        Object rolesValue = request.get("roles");
        if (rolesValue instanceof java.util.List<?> roles && !roles.isEmpty() && roles.get(0) != null) {
            return String.valueOf(roles.get(0));
        }
        return defaultRole;
    }
}

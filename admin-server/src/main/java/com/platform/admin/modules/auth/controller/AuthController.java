package com.platform.admin.modules.auth.controller;

import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.service.UserService;
import com.platform.admin.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    public AuthController(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String username = request.get("username");
        String password = request.get("password");
        
        User user = userService.login(username, password);
        if (user == null) {
            return Result.error(1001, "用户名或密码错误");
        }

        if ("DISABLED".equals(user.getStatus())) { 
            return Result.error(1002, "账号已被禁用");
        }
        
        String accessToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        String refreshToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        
        String ip = getClientIp(httpRequest);
        userService.updateLoginInfo(user.getObjectId(), ip);
        
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("expiresIn", 7200);
        
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        Result<Void> result = Result.success(null);
        result.setMessage("登出成功");
        return result;
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String nickname = (String) request.get("nickname");
        String email = (String) request.get("email");
        String phone = (String) request.get("phone");

        User existing = userService.getByUsername(username);
        if (existing != null) {
            return Result.error(ErrorCode.CONFLICT, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);

        User registeredUser = userService.register(user);

        Map<String, Object> data = new HashMap<>();
        data.put("objectId", registeredUser.getObjectId());
        data.put("username", registeredUser.getUsername());
        data.put("nickname", registeredUser.getNickname());
        data.put("email", registeredUser.getEmail());
        data.put("phone", registeredUser.getPhone());
        data.put("status", registeredUser.getStatus());
        data.put("roles", new String[]{registeredUser.getUserType()});
        data.put("createTime", registeredUser.getCreateTime());

        return Result.success(data);
    }

    @GetMapping("/current-user")
    public Result<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String accessToken = token.substring(7);
            String userId = jwtProvider.getUserIdFromToken(accessToken);
            
            User user = userService.getById(userId);
            if (user != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("objectId", user.getObjectId());
                data.put("username", user.getUsername());
                data.put("nickname", user.getNickname());
                data.put("avatar", user.getAvatar());
                data.put("roles", new String[]{user.getUserType()});
                data.put("permissions", new String[]{"user:read", "user:write", "role:read", "role:write", "audit:read", "audit:write", "config:read", "config:write"});
                return Result.success(data);
            }
        }
        return Result.error(ErrorCode.UNAUTHORIZED, "未认证");
    }

    @PostMapping("/refresh-token")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        String userId = jwtProvider.getUserIdFromToken(refreshToken);
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED, "无效的刷新令牌");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        
        String newAccessToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        String newRefreshToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", newAccessToken);
        data.put("refreshToken", newRefreshToken);
        data.put("expiresIn", 7200);
        
        return Result.success(data);
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
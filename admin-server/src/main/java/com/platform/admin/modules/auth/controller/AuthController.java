package com.platform.admin.modules.auth.controller;

import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.log.support.LogPermissionResolver;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.service.UserService;
import com.platform.admin.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final LogPermissionResolver logPermissionResolver;

    public AuthController(UserService userService,
                          JwtProvider jwtProvider,
                          LogPermissionResolver logPermissionResolver) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.logPermissionResolver = logPermissionResolver;
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
        return Result.success(null);
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
                List<String> permissions = new ArrayList<>(List.of(
                        "user:read", "user:write", "role:read", "role:write",
                        "audit:read", "audit:write", "config:read", "config:write"
                ));
                permissions.addAll(logPermissionResolver.resolve(user));
                data.put("permissions", permissions.toArray(new String[0]));
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
package com.platform.admin.modules.auth.controller;

import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.common.log.SecurityLogEventType;
import com.platform.admin.common.log.SecurityLogWriter;
import com.platform.admin.common.util.ClientIpUtils;
import com.platform.admin.modules.log.support.LogPermissionResolver;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.service.UserService;
import com.platform.admin.security.JwtProvider;
import com.platform.admin.security.SecurityUtil;
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
    private final SecurityLogWriter securityLogWriter;
    private final SecurityUtil securityUtil;

    public AuthController(UserService userService,
                          JwtProvider jwtProvider,
                          LogPermissionResolver logPermissionResolver,
                          SecurityLogWriter securityLogWriter,
                          SecurityUtil securityUtil) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.logPermissionResolver = logPermissionResolver;
        this.securityLogWriter = securityLogWriter;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String username = request.get("username");
        String password = request.get("password");
        
        User user = userService.login(username, password);
        String ip = ClientIpUtils.resolve(httpRequest);
        if (user == null) {
            securityLogWriter.writeSecurityAccess(
                    null, SecurityLogEventType.LOGIN, ip, "/api/v1/auth/login", "POST", "FAILED",
                    Map.of("username", username == null ? "" : username)
            );
            return Result.error(1001, "用户名或密码错误");
        }
        
        if ("DISABLED".equals(user.getStatus())) {
            securityLogWriter.writeSecurityAccess(
                    user.getObjectId(), SecurityLogEventType.LOGIN, ip, "/api/v1/auth/login", "POST", "DENIED",
                    Map.of("username", user.getUsername())
            );
            return Result.error(1002, "账号已被禁用");
        }
        
        String accessToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        String refreshToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        
        userService.updateLoginInfo(user.getObjectId(), ip);
        securityLogWriter.writeSecurityAccess(
                user.getObjectId(), SecurityLogEventType.LOGIN, ip, "/api/v1/auth/login", "POST", "SUCCESS",
                Map.of("username", user.getUsername())
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("expiresIn", jwtProvider.getAccessTokenTtlSeconds());
        
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        try {
            var currentUser = securityUtil.getCurrentUser();
            securityLogWriter.writeSecurityAccess(
                    currentUser.objectId(),
                    SecurityLogEventType.LOGOUT,
                    ClientIpUtils.resolve(httpRequest),
                    "/api/v1/auth/logout",
                    "POST",
                    "SUCCESS",
                    Map.of()
            );
        } catch (Exception ignored) {
            // 未登录登出仍返回成功
        }
        return Result.success(null);
    }

    @GetMapping("/current-user")
    public Result<Map<String, Object>> getCurrentUser() {
        String userId = securityUtil.getCurrentUser().objectId();
        User user = userService.getById(userId);
        if (user != null && !"DISABLED".equals(user.getStatus())) {
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
        return Result.error(ErrorCode.UNAUTHORIZED, "未认证");
    }

    @PostMapping("/refresh-token")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank() || !jwtProvider.validateToken(refreshToken)) {
            return Result.error(ErrorCode.UNAUTHORIZED, "无效的刷新令牌");
        }
        
        String userId = jwtProvider.getUserIdFromToken(refreshToken);
        if (userId == null || userId.isBlank()) {
            return Result.error(ErrorCode.UNAUTHORIZED, "无效的刷新令牌");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        if ("DISABLED".equals(user.getStatus())) {
            return Result.error(ErrorCode.UNAUTHORIZED, "账号已被禁用");
        }
        
        String newAccessToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        String newRefreshToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", newAccessToken);
        data.put("refreshToken", newRefreshToken);
        data.put("expiresIn", jwtProvider.getAccessTokenTtlSeconds());
        
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String username = request.get("username");
        String password = request.get("password");
        String phone = request.get("phone");
        String email = request.get("email");
        String nickname = request.get("nickname");
        
        if (username == null || username.isBlank()) {
            return Result.error(400, "用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            return Result.error(400, "密码不能为空");
        }
        if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
            return Result.error(400, "手机号和邮箱至少填写一个");
        }
        
        User existingUser = userService.getUserByUsername(username);
        if (existingUser != null) {
            return Result.error(409, "用户名已存在");
        }
        
        if (phone != null && !phone.isBlank()) {
            existingUser = userService.getUserByPhone(phone);
            if (existingUser != null) {
                return Result.error(409, "手机号已被注册");
            }
        }
        
        if (email != null && !email.isBlank()) {
            existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                return Result.error(409, "邮箱已被注册");
            }
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password);
        user.setPhone(phone);
        user.setEmail(email);
        user.setNickname(nickname != null && !nickname.isBlank() ? nickname : username);
        user.setUserType("USER");
        user.setStatus("ENABLED");
        
        userService.register(user);
        
        String ip = ClientIpUtils.resolve(httpRequest);
        securityLogWriter.writeSecurityAccess(
                user.getObjectId(), SecurityLogEventType.REGISTER, ip, "/api/v1/auth/register", "POST", "SUCCESS",
                Map.of("username", user.getUsername())
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", user.getObjectId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("phone", user.getPhone() != null ? user.getPhone() : "");
        data.put("email", user.getEmail() != null ? user.getEmail() : "");
        data.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        data.put("createTime", user.getCreateTime());
        
        return Result.success(data);
    }
}

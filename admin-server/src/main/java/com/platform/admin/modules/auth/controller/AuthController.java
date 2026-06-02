package com.platform.admin.modules.auth.controller;

import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PublicUrlResolver;
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

import com.platform.admin.modules.auth.dto.PasswordUpdateRequest;
import jakarta.validation.Valid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.admin.modules.auth.dto.HuaweiLoginRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PublicUrlResolver publicUrlResolver;

    public AuthController(UserService userService,
                          JwtProvider jwtProvider,
                          LogPermissionResolver logPermissionResolver,
                          SecurityLogWriter securityLogWriter,
                          SecurityUtil securityUtil,
                            PublicUrlResolver publicUrlResolver) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.logPermissionResolver = logPermissionResolver;
        this.securityLogWriter = securityLogWriter;
        this.securityUtil = securityUtil;
        this.publicUrlResolver = publicUrlResolver;
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
    public Result<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String userId = securityUtil.getCurrentUser().objectId();
        User user = userService.getById(userId);
        if (user != null && !"DISABLED".equals(user.getStatus())) {
            Map<String, Object> data = new HashMap<>();
            data.put("objectId", user.getObjectId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname() == null ? user.getUsername() : user.getNickname());
            data.put("avatar", publicUrlResolver.toPublicUrl(user.getAvatar(), request));
            data.put("bio", user.getBio() == null ? "" : user.getBio());
            data.put("phone", user.getPhone() == null ? "" : user.getPhone());
            data.put("email", user.getEmail() == null ? "" : user.getEmail());
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

        @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Valid PasswordUpdateRequest request) {
        String userId = securityUtil.getCurrentUser().objectId();

        boolean changed = userService.changePassword(
                userId,
                request.getOldPassword(),
                request.getNewPassword()
        );

        if (!changed) {
            return Result.error(ErrorCode.BAD_REQUEST, "原密码错误");
        }

        Result<Void> result = Result.success(null);
        result.setMessage("密码修改成功");
        return result;
    }

        @PostMapping("/huawei-login")
    public Result<Map<String, Object>> huaweiLogin(
            @RequestBody HuaweiLoginRequest request,
            HttpServletRequest httpRequest) {

        String huaweiSubject = resolveHuaweiSubject(request);

        if (huaweiSubject == null || huaweiSubject.isBlank()) {
            return Result.error(ErrorCode.BAD_REQUEST, "华为登录凭证无效");
        }

        String username = "huawei_" + sha256(huaweiSubject).substring(0, 16);

        User user = userService.getByUsername(username);

        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPasswordHash(UUID.randomUUID().toString());
            user.setNickname(hasText(request.getNickname()) ? request.getNickname() : "华为用户");
            user.setAvatar(hasText(request.getAvatar()) ? request.getAvatar() : "");
            user.setEmail(hasText(request.getEmail()) ? request.getEmail() : "");
            user.setPhone("");
            user.setBio("掌上博物馆用户");
            user.setUserType("MOBILE");

            user = userService.register(user);
        }

        if ("DISABLED".equals(user.getStatus())) {
            return Result.error(ErrorCode.UNAUTHORIZED, "账号已被禁用");
        }

        String ip = ClientIpUtils.resolve(httpRequest);
        userService.updateLoginInfo(user.getObjectId(), ip);

        String accessToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());
        String refreshToken = jwtProvider.generateToken(user.getObjectId(), user.getUserType());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("objectId", user.getObjectId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname() == null ? user.getUsername() : user.getNickname());
        userInfo.put("avatar", publicUrlResolver.toPublicUrl(user.getAvatar(), httpRequest));
        userInfo.put("bio", user.getBio() == null ? "" : user.getBio());
        userInfo.put("phone", user.getPhone() == null ? "" : user.getPhone());
        userInfo.put("email", user.getEmail() == null ? "" : user.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("expiresIn", jwtProvider.getAccessTokenTtlSeconds());
        data.put("user", userInfo);

        return Result.success(data);
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

    private String resolveHuaweiSubject(HuaweiLoginRequest request) {
    if (request == null) {
        return null;
    }

    if (hasText(request.getUnionId())) {
        return "union:" + request.getUnionId().trim();
    }

    if (hasText(request.getOpenId())) {
        return "open:" + request.getOpenId().trim();
    }

    if (hasText(request.getIdToken())) {
        String subject = parseSubjectFromIdTokenWithoutVerify(request.getIdToken());
        if (hasText(subject)) {
            return "idtoken:" + subject;
        }
    }

        /*
        * 注意：
        * 这里用 authorizationCode 做兜底只适合课程项目联调。
        * 正式版本不能直接把 authorizationCode 当用户唯一身份，
        * 因为 authorizationCode 通常是一次性的。
        * 正式版本应拿 authorizationCode 到华为服务器换取 id_token / openId / unionId。
        */
        if (hasText(request.getAuthorizationCode())) {
            return "code:" + request.getAuthorizationCode().trim();
        }

        return null;
    }

    private String parseSubjectFromIdTokenWithoutVerify(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(payloadBytes, StandardCharsets.UTF_8);

            JsonNode node = objectMapper.readTree(payload);

            if (node.hasNonNull("sub")) {
                return node.get("sub").asText();
            }

            if (node.hasNonNull("open_id")) {
                return node.get("open_id").asText();
            }

            if (node.hasNonNull("union_id")) {
                return node.get("union_id").asText();
            }

            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (Exception e) {
            return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8))
                    .toString()
                    .replace("-", "");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}

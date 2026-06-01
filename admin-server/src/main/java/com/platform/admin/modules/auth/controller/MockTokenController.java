package com.platform.admin.modules.auth.controller;

import com.platform.admin.common.Result;
import com.platform.admin.modules.auth.vo.MockTokenVO;
import com.platform.admin.security.JwtProvider;
import com.platform.admin.security.UserType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在 {@code app.auth.mock-token.enabled=true} 时注册：签发测试用 JWT，用于未对接登录流程时的接口联调。
 * <p>
 * 用法示例：{@code GET /api/v1/auth/mock-token?userId=admin-user-1&userType=ADMIN}
 */
@ConditionalOnProperty(prefix = "app.auth.mock-token", name = "enabled", havingValue = "true")
@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class MockTokenController {

    private final JwtProvider jwtProvider;

    public MockTokenController(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * 生成 Mock Access Token（ claims 含 subject=userId、userType ）。
     *
     * @param userId   模拟用户主键，默认 mock-user-1
     * @param userType 角色编码，默认 ADMIN；与 {@code user.user_type} / {@code role.role_code} 对齐
     */
    @GetMapping("/mock-token")
    public Result<MockTokenVO> mockToken(
            @RequestParam(defaultValue = "admin-user-1") String userId,
            @RequestParam(defaultValue = "ADMIN") String userType) {
        UserType resolved = UserType.fromValue(userType);
        String token = jwtProvider.generateToken(userId, resolved.name());
        long ttl = jwtProvider.getAccessTokenTtlSeconds();
        return Result.success(new MockTokenVO(token, ttl, userId, resolved.name()));
    }
}

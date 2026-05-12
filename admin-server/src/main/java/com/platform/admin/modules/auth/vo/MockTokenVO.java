package com.platform.admin.modules.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * dev 环境 Mock 登录返回，便于联调携带 Bearer Token。
 */
@Data
@AllArgsConstructor
public class MockTokenVO {
    private String accessToken;
    private long expiresInSeconds;
    private String userId;
    private String userType;
}

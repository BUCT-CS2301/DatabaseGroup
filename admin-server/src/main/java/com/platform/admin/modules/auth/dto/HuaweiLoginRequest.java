package com.platform.admin.modules.auth.dto;

import lombok.Data;

@Data
public class HuaweiLoginRequest {

    /**
     * 华为 Account Kit 返回的授权码。
     * 正式版本应由后端拿 authorizationCode 去华为服务器换 token。
     */
    private String authorizationCode;

    /**
     * 华为 Account Kit 返回的 idToken。
     * 正式版本应校验签名、issuer、audience、过期时间。
     */
    private String idToken;

    /**
     * 前端如果已经能拿到 openId / unionId，可以直接传给后端用于联调。
     * unionId 优先级高于 openId。
     */
    private String openId;
    private String unionId;

    private String nickname;
    private String avatar;
    private String email;
}
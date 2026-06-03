package com.platform.admin.modules.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileVO {
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
}
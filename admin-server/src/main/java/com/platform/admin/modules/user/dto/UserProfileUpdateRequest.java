package com.platform.admin.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @Size(max = 100, message = "nickname长度不能超过100")
    private String nickname;

    @Size(max = 500, message = "bio长度不能超过500")
    private String bio;

    @Size(max = 30, message = "phone长度不能超过30")
    private String phone;

    @Email(message = "email格式不正确")
    @Size(max = 200, message = "email长度不能超过200")
    private String email;
}
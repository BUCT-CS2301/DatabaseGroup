package com.platform.admin.security;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    /**
     * 获取当前登录用户。
     *
     * @return 当前用户
     */
    public AuthUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未认证或Token失效");
        }
        return authUser;
    }

    /**
     * 校验当前用户是否具备管理员写权限。
     */
    public void requireAdminWritePermission() {
        AuthUser currentUser = getCurrentUser();
        if (currentUser.userType() != UserType.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无操作权限");
        }
    }
}

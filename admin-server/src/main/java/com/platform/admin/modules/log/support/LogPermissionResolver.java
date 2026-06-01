package com.platform.admin.modules.log.support;

import com.platform.admin.modules.user.entity.User;
import com.platform.admin.security.UserType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据用户类型解析日志模块权限。
 */
@Component
public class LogPermissionResolver {

    /**
     * 解析用户具备的日志相关权限。
     *
     * @param user 用户实体
     * @return 权限编码列表
     */
    public List<String> resolve(User user) {
        if (user == null || "DISABLED".equals(user.getStatus())) {
            return List.of();
        }
        UserType userType = UserType.fromValue(user.getUserType());
        List<String> permissions = new ArrayList<>();
        switch (userType) {
            case ADMIN -> {
                permissions.add(LogPermissions.READ);
                permissions.add(LogPermissions.EXPORT);
            }
            case KNOWLEDGE_SERVICE, AUDITOR -> permissions.add(LogPermissions.READ);
            default -> {
            }
        }
        return permissions;
    }
}

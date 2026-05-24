package com.platform.admin.modules.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.admin.modules.role.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {
    List<Role> getAllRoles();
    Role getByCode(String code);
}
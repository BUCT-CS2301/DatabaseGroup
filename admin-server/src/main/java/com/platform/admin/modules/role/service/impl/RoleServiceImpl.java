package com.platform.admin.modules.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.admin.modules.role.entity.Role;
import com.platform.admin.modules.role.mapper.RoleMapper;
import com.platform.admin.modules.role.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> getAllRoles() {
        return list();
    }

    @Override
    public Role getByCode(String code) {
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", code);
        return getOne(wrapper);
    }
}
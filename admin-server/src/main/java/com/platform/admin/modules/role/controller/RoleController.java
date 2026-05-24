package com.platform.admin.modules.role.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.role.entity.Role;
import com.platform.admin.modules.role.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Result<IPage<Role>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Role> pageRequest = new Page<>(page, size);
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("role_name", keyword).or().like("role_code", keyword);
        }
        wrapper.orderByDesc("create_time");
        IPage<Role> result = roleService.page(pageRequest, wrapper);
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable String id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return Result.success(role);
    }

    @GetMapping("/code/{code}")
    public Result<Role> getByCode(@PathVariable String code) {
        Role role = roleService.getByCode(code);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return Result.success(role);
    }

    @PostMapping
    public Result<Role> create(@RequestBody Role role) {
        Role existing = roleService.getByCode(role.getRoleCode());
        if (existing != null) {
            return Result.error(ErrorCode.CONFLICT, "角色编码已存在");
        }
        role.setObjectId(UUID.randomUUID().toString());
        role.setIsSystem(0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleService.save(role);
        return Result.success(role);
    }

    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable String id, @RequestBody Role role) {
        Role existing = roleService.getById(id);
        if (existing == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        if (!existing.getRoleCode().equals(role.getRoleCode())) {
            Role check = roleService.getByCode(role.getRoleCode());
            if (check != null && !check.getObjectId().equals(id)) {
                return Result.error(ErrorCode.CONFLICT, "角色编码已存在");
            }
        }
        role.setObjectId(id);
        role.setIsSystem(existing.getIsSystem());
        role.setUpdateTime(LocalDateTime.now());
        roleService.updateById(role);
        return Result.success(role);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        if (role.getIsSystem() == 1) {
            return Result.error(ErrorCode.FORBIDDEN, "系统角色不能删除");
        }
        roleService.removeById(id);
        return Result.success(null);
    }
}
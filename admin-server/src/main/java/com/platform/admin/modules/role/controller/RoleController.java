package com.platform.admin.modules.role.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.role.entity.Role;
import com.platform.admin.modules.role.service.RoleService;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;
    private final UserService userService;

    public RoleController(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Role> pageRequest = new Page<>(page, pageSize);
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("role_name", keyword).or().like("role_code", keyword);
        }
        wrapper.orderByDesc("create_time");
        IPage<Role> result = roleService.page(pageRequest, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords().stream().map(role -> {
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("objectId", role.getObjectId());
            roleMap.put("roleName", role.getRoleName());
            roleMap.put("roleCode", role.getRoleCode());
            roleMap.put("description", role.getDescription());
            roleMap.put("permissions", splitPermissions(role.getPermissions()));
            roleMap.put("userCount", userService.count(new QueryWrapper<User>()
                    .eq("user_type", role.getRoleCode())
                    .eq("is_deleted", 0)));
            roleMap.put("isSystem", role.getIsSystem() == 1);
            roleMap.put("createTime", role.getCreateTime());
            roleMap.put("updateTime", role.getUpdateTime());
            return roleMap;
        }).toList());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        
        return Result.success(data);
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        List<Map<String, Object>> result = roles.stream().map(role -> {
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("objectId", role.getObjectId());
            roleMap.put("roleName", role.getRoleName());
            roleMap.put("roleCode", role.getRoleCode());
            roleMap.put("permissions", splitPermissions(role.getPermissions()));
            return roleMap;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/{objectId}")
    public Result<Map<String, Object>> getById(@PathVariable String objectId) {
        Role role = roleService.getById(objectId);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", role.getObjectId());
        data.put("roleName", role.getRoleName());
        data.put("roleCode", role.getRoleCode());
        data.put("description", role.getDescription());
        data.put("permissions", splitPermissions(role.getPermissions()));
        data.put("userCount", userService.count(new QueryWrapper<User>().eq("user_type", role.getRoleCode()).eq("is_deleted", 0)));
        data.put("isSystem", role.getIsSystem() == 1);
        data.put("createTime", role.getCreateTime());
        data.put("updateTime", role.getUpdateTime());
        
        return Result.success(data);
    }

    @GetMapping("/code/{roleCode}")
    public Result<Map<String, Object>> getByCode(@PathVariable String roleCode) {
        Role role = roleService.getByCode(roleCode);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", role.getObjectId());
        data.put("roleName", role.getRoleName());
        data.put("roleCode", role.getRoleCode());
        data.put("description", role.getDescription());
        data.put("isSystem", role.getIsSystem() == 1);
        
        return Result.success(data);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> request) {
        String roleName = (String) request.get("roleName");
        String roleCode = (String) request.get("roleCode");
        String description = (String) request.get("description");
        String permissions = joinPermissions(request.get("permissions"));
        
        Role existing = roleService.getByCode(roleCode);
        if (existing != null) {
            return Result.error(2001, "角色名称已存在");
        }
        
        Role role = new Role();
        role.setObjectId(UUID.randomUUID().toString());
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setDescription(description);
        role.setPermissions(permissions);
        role.setIsSystem(0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        
        roleService.save(role);
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", role.getObjectId());
        data.put("roleName", role.getRoleName());
        data.put("roleCode", role.getRoleCode());
        data.put("description", role.getDescription());
        data.put("permissions", splitPermissions(role.getPermissions()));
        data.put("isSystem", false);
        data.put("createTime", role.getCreateTime());
        
        return Result.success(data);
    }

    @PutMapping("/{objectId}")
    public Result<Map<String, Object>> update(@PathVariable String objectId, @RequestBody Map<String, Object> request) {   
        Role existing = roleService.getById(objectId);
        if (existing == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        
        String roleName = (String) request.get("roleName");
        String description = (String) request.get("description");
        String permissions = joinPermissions(request.get("permissions"));
        
        existing.setRoleName(roleName);
        existing.setDescription(description);
        existing.setPermissions(permissions);
        existing.setUpdateTime(LocalDateTime.now());
        
        roleService.updateById(existing);
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", existing.getObjectId());
        data.put("roleName", existing.getRoleName());
        data.put("roleCode", existing.getRoleCode());
        data.put("description", existing.getDescription());
        data.put("permissions", splitPermissions(existing.getPermissions()));
        data.put("isSystem", existing.getIsSystem() == 1);
        data.put("updateTime", existing.getUpdateTime());
        
        return Result.success(data);
    }

    @DeleteMapping("/{objectId}")
    public Result<Void> delete(@PathVariable String objectId) {
        Role role = roleService.getById(objectId);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        if (role.getIsSystem() == 1) {
            return Result.error(2002, "角色不可删除");
        }
        roleService.removeById(objectId);
        return Result.success(null);
    }

    @GetMapping("/{objectId}/permissions")
    public Result<List<Map<String, Object>>> getRolePermissions(@PathVariable String objectId) {
        Role role = roleService.getById(objectId);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        List<Map<String, Object>> permissions = splitPermissions(role.getPermissions()).stream().map(permission -> {
            Map<String, Object> item = new HashMap<>();
            item.put("code", permission);
            item.put("name", permission);
            return item;
        }).toList();
        return Result.success(permissions);
    }

    @PutMapping("/{objectId}/permissions")
    public Result<Void> setRolePermissions(@PathVariable String objectId, @RequestBody Map<String, List<String>> request) {
        Role role = roleService.getById(objectId);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        role.setPermissions(String.join(",", request.getOrDefault("permissions", List.of())));
        role.setUpdateTime(LocalDateTime.now());
        roleService.updateById(role);
        return Result.success(null);
    }

    @GetMapping("/permissions")
    public Result<List<Map<String, Object>>> getAllPermissions() {
        List<Map<String, Object>> permissions = permissionCatalog().stream().map(permission -> {
            Map<String, Object> item = new HashMap<>();
            item.put("code", permission[0]);
            item.put("name", permission[1]);
            return item;
        }).toList();
        return Result.success(permissions);
    }

    @GetMapping("/{objectId}/users")
    public Result<Map<String, Object>> getRoleUsers(
            @PathVariable String objectId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Role role = roleService.getById(objectId);
        if (role == null) {
            return Result.error(ErrorCode.NOT_FOUND, "角色不存在");
        }
        Page<User> pageRequest = new Page<>(page, pageSize);
        IPage<User> result = userService.page(pageRequest, new QueryWrapper<User>()
                .select(User.class, info -> !info.getColumn().equals("password_hash"))
                .eq("user_type", role.getRoleCode())
                .eq("is_deleted", 0)
                .orderByDesc("create_time"));

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords().stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("objectId", user.getObjectId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("status", user.getStatus());
            userMap.put("createTime", user.getCreateTime());
            return userMap;
        }).toList());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        return Result.success(data);
    }

    private List<String> splitPermissions(String permissions) {
        if (permissions == null || permissions.isBlank()) {
            return List.of();
        }
        return Arrays.stream(permissions.split(","))
                .map(String::trim)
                .filter(permission -> !permission.isEmpty())
                .toList();
    }

    private String joinPermissions(Object permissions) {
        if (permissions instanceof List<?> permissionList) {
            return permissionList.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        if (permissions instanceof String permissionString) {
            return permissionString;
        }
        return "";
    }

    private List<String[]> permissionCatalog() {
        return List.of(
                new String[]{"dashboard:view", "查看监控看板"},
                new String[]{"user:read", "查看用户"},
                new String[]{"user:write", "管理用户"},
                new String[]{"role:read", "查看角色"},
                new String[]{"role:write", "管理角色"},
                new String[]{"audit:read", "查看审核内容"},
                new String[]{"audit:write", "处理内容审核"},
                new String[]{"data:read", "查看数据"},
                new String[]{"data:write", "管理数据"},
                new String[]{"backup:read", "查看备份"},
                new String[]{"backup:write", "备份恢复"},
                new String[]{"log:read", "查看日志"},
                new String[]{"settings:read", "查看配置"},
                new String[]{"settings:write", "管理配置"}
        );
    }
}

package com.platform.admin.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.user.dto.PrivacyUpdateRequest;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.entity.UserPrivacySettingEntity;
import com.platform.admin.modules.user.mapper.UserMapper;
import com.platform.admin.modules.user.mapper.UserPrivacySettingMapper;
import com.platform.admin.modules.user.vo.UserPrivacySettingVO;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RequestMapping("/api/v1/users")
@RestController
public class UserPrivacyController {

    private final UserMapper userMapper;
    private final UserPrivacySettingMapper privacyMapper;
    private final SecurityUtil securityUtil;

    public UserPrivacyController(
            UserMapper userMapper,
            UserPrivacySettingMapper privacyMapper,
            SecurityUtil securityUtil) {
        this.userMapper = userMapper;
        this.privacyMapper = privacyMapper;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/{username}/privacy")
    public Result<UserPrivacySettingVO> getPrivacy(@PathVariable String username) {
        User user = requireSelfUser(username);
        UserPrivacySettingEntity setting = getOrCreateSetting(user.getObjectId());
        return Result.success(toVO(setting));
    }

    @PutMapping("/{username}/privacy")
    public Result<UserPrivacySettingVO> updatePrivacy(
            @PathVariable String username,
            @RequestBody PrivacyUpdateRequest request) {
        User user = requireSelfUser(username);
        UserPrivacySettingEntity setting = getOrCreateSetting(user.getObjectId());

        if (request.getFavoritesVisible() != null) {
            setting.setFavoritesVisible(boolToInt(request.getFavoritesVisible()));
        }
        if (request.getLikesVisible() != null) {
            setting.setLikesVisible(boolToInt(request.getLikesVisible()));
        }
        if (request.getCommentsVisible() != null) {
            setting.setCommentsVisible(boolToInt(request.getCommentsVisible()));
        }
        if (request.getUploadsVisible() != null) {
            setting.setUploadsVisible(boolToInt(request.getUploadsVisible()));
        }

        setting.setUpdateTime(LocalDateTime.now());
        privacyMapper.updateById(setting);

        return Result.success(toVO(setting));
    }

    private User requireSelfUser(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0));

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        AuthUser currentUser = securityUtil.getCurrentUser();
        if (!user.getObjectId().equals(currentUser.objectId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能修改自己的隐私设置");
        }

        return user;
    }

    private UserPrivacySettingEntity getOrCreateSetting(String userId) {
        UserPrivacySettingEntity setting = privacyMapper.selectOne(
                new LambdaQueryWrapper<UserPrivacySettingEntity>()
                        .eq(UserPrivacySettingEntity::getUserId, userId)
        );

        if (setting != null) {
            return setting;
        }

        LocalDateTime now = LocalDateTime.now();

        UserPrivacySettingEntity created = new UserPrivacySettingEntity();
        created.setObjectId(UUID.randomUUID().toString());
        created.setUserId(userId);
        created.setFavoritesVisible(1);
        created.setLikesVisible(1);
        created.setCommentsVisible(1);
        created.setUploadsVisible(1);
        created.setCreateTime(now);
        created.setUpdateTime(now);

        privacyMapper.insert(created);
        return created;
    }

    private UserPrivacySettingVO toVO(UserPrivacySettingEntity setting) {
        return new UserPrivacySettingVO(
                intToBool(setting.getFavoritesVisible()),
                intToBool(setting.getLikesVisible()),
                intToBool(setting.getCommentsVisible()),
                intToBool(setting.getUploadsVisible())
        );
    }

    private Integer boolToInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private Boolean intToBool(Integer value) {
        return value != null && value == 1;
    }
}
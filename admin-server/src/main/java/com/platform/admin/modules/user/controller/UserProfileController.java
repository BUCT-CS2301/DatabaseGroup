package com.platform.admin.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import com.platform.admin.modules.user.vo.UserProfileVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/users")
@RestController
public class UserProfileController {

    private final UserMapper userMapper;

    public UserProfileController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/{username}/profile")
    public Result<UserProfileVO> getUserProfile(@PathVariable String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0));

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        String nickname = user.getNickname() == null ? user.getUsername() : user.getNickname();
        String avatar = user.getAvatar() == null ? "" : user.getAvatar();

        return Result.success(new UserProfileVO(
                user.getUsername(),
                nickname,
                avatar,
                ""
        ));
    }
}
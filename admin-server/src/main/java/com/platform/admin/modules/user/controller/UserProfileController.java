package com.platform.admin.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.user.dto.UserProfileUpdateRequest;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import com.platform.admin.modules.user.vo.AvatarUploadVO;
import com.platform.admin.modules.user.vo.UserProfileVO;
import com.platform.admin.security.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@RequestMapping("/api/v1/users")
@RestController
public class UserProfileController {

    private static final long MAX_AVATAR_BYTES = 5 * 1024 * 1024L;

    private final UserMapper userMapper;
    private final SecurityUtil securityUtil;

    @Value("${app.relics.image-public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public UserProfileController(UserMapper userMapper, SecurityUtil securityUtil) {
        this.userMapper = userMapper;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/{username}/profile")
    public Result<UserProfileVO> getUserProfile(@PathVariable String username) {
        User user = requireUserByUsername(username);

        String nickname = user.getNickname() == null ? user.getUsername() : user.getNickname();
        String avatar = user.getAvatar() == null ? "" : user.getAvatar();
        String bio = user.getBio() == null ? "" : user.getBio();

        return Result.success(new UserProfileVO(
                user.getUsername(),
                nickname,
                avatar,
                bio
        ));
    }

    @PutMapping("/me/profile")
    public Result<UserProfileVO> updateMyProfile(@RequestBody @Valid UserProfileUpdateRequest request) {
        User user = requireCurrentUser();

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim());
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return Result.success(new UserProfileVO(
                user.getUsername(),
                user.getNickname() == null ? user.getUsername() : user.getNickname(),
                user.getAvatar() == null ? "" : user.getAvatar(),
                user.getBio() == null ? "" : user.getBio()
        ));
    }

    @PostMapping("/me/avatar")
    public Result<AvatarUploadVO> uploadMyAvatar(@RequestPart("file") MultipartFile file) throws IOException {
        User user = requireCurrentUser();

        if (file == null || file.isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "头像文件不能为空");
        }

        if (file.getSize() > MAX_AVATAR_BYTES) {
            return Result.error(ErrorCode.PAYLOAD_TOO_LARGE, "头像文件不能超过5MB");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);

        if (!isAllowedImageExt(ext)) {
            return Result.error(ErrorCode.BAD_REQUEST, "仅支持jpg、jpeg、png、webp格式");
        }

        Path uploadDir = Paths.get("uploads", "avatars").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String filename = user.getObjectId() + "-" + UUID.randomUUID() + "." + ext;
        Path target = uploadDir.resolve(filename).normalize();

        file.transferTo(target.toFile());

        String avatarUrl = normalizeBaseUrl(publicBaseUrl) + "/uploads/avatars/" + filename;

        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        Result<AvatarUploadVO> result = Result.success(new AvatarUploadVO(avatarUrl));
        result.setMessage("头像上传成功");
        return result;
    }

    private User requireCurrentUser() {
        String userId = securityUtil.getCurrentUser().objectId();
        User user = userMapper.selectById(userId);

        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        return user;
    }

    private User requireUserByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0));

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        return user;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedImageExt(String ext) {
        return "jpg".equals(ext)
                || "jpeg".equals(ext)
                || "png".equals(ext)
                || "webp".equals(ext);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "http://localhost:8080";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
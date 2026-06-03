package com.platform.admin.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PublicUrlResolver;
import com.platform.admin.common.Result;
import com.platform.admin.modules.user.dto.UserProfileUpdateRequest;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import com.platform.admin.modules.user.vo.AvatarUploadVO;
import com.platform.admin.modules.user.vo.UserProfileVO;
import com.platform.admin.security.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

import com.platform.admin.modules.user.dto.AvatarBase64UploadRequest;

import java.util.Base64;

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
    private final PublicUrlResolver publicUrlResolver;

    public UserProfileController(
            UserMapper userMapper,
            SecurityUtil securityUtil,
            PublicUrlResolver publicUrlResolver) {
        this.userMapper = userMapper;
        this.securityUtil = securityUtil;
        this.publicUrlResolver = publicUrlResolver;
    }

    @GetMapping("/{username}/profile")
    public Result<UserProfileVO> getUserProfile(
            @PathVariable String username,
            HttpServletRequest request) {
        User user = requireUserByUsername(username);

        String nickname = user.getNickname() == null ? user.getUsername() : user.getNickname();
        String avatar = publicUrlResolver.toPublicUrl(user.getAvatar(), request);
        String bio = user.getBio() == null ? "" : user.getBio();

        return Result.success(new UserProfileVO(
                user.getUsername(),
                nickname,
                avatar,
                bio
        ));
    }

    @PutMapping("/me/profile")
    public Result<UserProfileVO> updateMyProfile(
            @RequestBody @Valid UserProfileUpdateRequest requestBody,
            HttpServletRequest request) {
        User user = requireCurrentUser();

        if (requestBody.getNickname() != null) {
            user.setNickname(requestBody.getNickname().trim());
        }
        if (requestBody.getBio() != null) {
            user.setBio(requestBody.getBio().trim());
        }
        if (requestBody.getPhone() != null) {
            user.setPhone(requestBody.getPhone().trim());
        }
        if (requestBody.getEmail() != null) {
            user.setEmail(requestBody.getEmail().trim());
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return Result.success(new UserProfileVO(
                user.getUsername(),
                user.getNickname() == null ? user.getUsername() : user.getNickname(),
                publicUrlResolver.toPublicUrl(user.getAvatar(), request),
                user.getBio() == null ? "" : user.getBio()
        ));
    }

    @PostMapping("/me/avatar")
    public Result<AvatarUploadVO> uploadMyAvatar(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        User user = requireCurrentUser();

        if (file == null || file.isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "头像文件不能为空");
        }

        if (file.getSize() > MAX_AVATAR_BYTES) {
            return Result.error(ErrorCode.BAD_REQUEST, "头像文件不能超过5MB");
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

        /*
         * 数据库只保存相对路径，不保存 localhost。
         * 返回给 App 时再通过 PublicUrlResolver 拼接完整可访问 URL。
         */
        String avatarPath = "/uploads/avatars/" + filename;

        user.setAvatar(avatarPath);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        String avatarUrl = publicUrlResolver.toPublicUrl(avatarPath, request);

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

    private String resolveImageExtension(String filename, String contentType) {
    String ext = getExtension(filename);

    if (isAllowedImageExt(ext)) {
        return ext;
    }

    if (!StringUtils.hasText(contentType)) {
        return "";
    }

    String normalized = contentType.trim().toLowerCase(Locale.ROOT);

    return switch (normalized) {
        case "image/jpeg", "image/jpg" -> "jpg";
        case "image/png" -> "png";
        case "image/webp" -> "webp";
        default -> "";
    };
}

    private boolean isAllowedImageExt(String ext) {
        return "jpg".equals(ext)
                || "jpeg".equals(ext)
                || "png".equals(ext)
                || "webp".equals(ext);
    }

        @PostMapping("/me/avatar-base64")
    public Result<AvatarUploadVO> uploadMyAvatarBase64(
            @RequestBody @Valid AvatarBase64UploadRequest requestBody,
            HttpServletRequest request) throws IOException {
        User user = requireCurrentUser();

        String rawBase64 = requestBody.getBase64();
        String contentType = requestBody.getContentType();
        String fileName = requestBody.getFileName();

        if (!StringUtils.hasText(rawBase64)) {
            return Result.error(ErrorCode.BAD_REQUEST, "base64内容不能为空");
        }

        // 兼容 data:image/jpeg;base64,xxxx 格式
        if (rawBase64.contains(",")) {
            String prefix = rawBase64.substring(0, rawBase64.indexOf(','));
            rawBase64 = rawBase64.substring(rawBase64.indexOf(',') + 1);

            if (!StringUtils.hasText(contentType)
                    && prefix.startsWith("data:")
                    && prefix.contains(";")) {
                contentType = prefix.substring(5, prefix.indexOf(';'));
            }
        }

        String ext = resolveImageExtension(fileName, contentType);

        if (!isAllowedImageExt(ext)) {
            return Result.error(ErrorCode.BAD_REQUEST, "仅支持jpg、jpeg、png、webp格式");
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(rawBase64.replaceAll("\\s+", ""));
        } catch (IllegalArgumentException e) {
            return Result.error(ErrorCode.BAD_REQUEST, "base64格式不正确");
        }

        if (imageBytes.length == 0) {
            return Result.error(ErrorCode.BAD_REQUEST, "图片内容不能为空");
        }

        if (imageBytes.length > MAX_AVATAR_BYTES) {
            return Result.error(ErrorCode.BAD_REQUEST, "头像文件不能超过5MB");
        }

        Path uploadDir = Paths.get("uploads", "avatars").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String filename = user.getObjectId() + "-" + UUID.randomUUID() + "." + ext;
        Path target = uploadDir.resolve(filename).normalize();

        Files.write(target, imageBytes);

        String avatarPath = "/uploads/avatars/" + filename;

        user.setAvatar(avatarPath);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        String avatarUrl = publicUrlResolver.toPublicUrl(avatarPath, request);

        Result<AvatarUploadVO> result = Result.success(new AvatarUploadVO(avatarUrl));
        result.setMessage("头像上传成功");
        return result;
    }

}
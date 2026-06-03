package com.platform.admin.modules.interaction.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PublicUrlResolver;
import com.platform.admin.common.Result;
import com.platform.admin.modules.artifact.mapper.ArtifactMapper;
import com.platform.admin.modules.interaction.entity.UserArtifactUploadEntity;
import com.platform.admin.modules.interaction.mapper.UserArtifactUploadMapper;
import com.platform.admin.modules.interaction.vo.ItemsPageVO;
import com.platform.admin.modules.interaction.vo.UserUploadVO;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1")
public class UserUploadController {

    private static final long MAX_UPLOAD_BYTES = 10 * 1024 * 1024L;

    private final UserArtifactUploadMapper uploadMapper;
    private final UserMapper userMapper;
    private final ArtifactMapper artifactMapper;
    private final SecurityUtil securityUtil;
    private final PublicUrlResolver publicUrlResolver;

    public UserUploadController(UserArtifactUploadMapper uploadMapper,
                                UserMapper userMapper,
                                ArtifactMapper artifactMapper,
                                SecurityUtil securityUtil,
                                PublicUrlResolver publicUrlResolver) {
        this.uploadMapper = uploadMapper;
        this.userMapper = userMapper;
        this.artifactMapper = artifactMapper;
        this.securityUtil = securityUtil;
        this.publicUrlResolver = publicUrlResolver;
    }

    @PostMapping("/artifacts/{artifactId}/uploads")
    public Result<UserUploadVO> uploadArtifactPhoto(@PathVariable String artifactId,
                                                   @RequestPart("file") MultipartFile file,
                                                   HttpServletRequest request) throws IOException {
        ensureArtifactExists(artifactId);

        AuthUser currentUser = securityUtil.getCurrentUser();

        if (file == null || file.isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "上传文件不能为空");
        }

        if (file.getSize() > MAX_UPLOAD_BYTES) {
            return Result.error(ErrorCode.BAD_REQUEST, "上传图片不能超过10MB");
        }

        String ext = getExtension(file.getOriginalFilename());

        if (!isAllowedImageExt(ext)) {
            return Result.error(ErrorCode.BAD_REQUEST, "仅支持jpg、jpeg、png、webp格式");
        }

        Path uploadDir = Paths.get("uploads", "user-photos").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String filename = currentUser.objectId() + "-" + UUID.randomUUID() + "." + ext;
        Path target = uploadDir.resolve(filename).normalize();

        file.transferTo(target.toFile());

        String imagePath = "/uploads/user-photos/" + filename;

        UserArtifactUploadEntity entity = new UserArtifactUploadEntity();
        entity.setObjectId(UUID.randomUUID().toString());
        entity.setUserId(currentUser.objectId());
        entity.setArtifactId(artifactId);
        entity.setImagePath(imagePath);
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());

        uploadMapper.insert(entity);

        UserUploadVO vo = uploadMapper.selectUserUploadDetail(currentUser.objectId(), entity.getObjectId());
        if (vo == null) {
            vo = new UserUploadVO();
            vo.setUploadId(entity.getObjectId());
            vo.setArtifactId(artifactId);
            vo.setImageUrl(imagePath);
            vo.setStatus("PENDING");
            vo.setCreateTime(entity.getCreateTime());
        }

        vo.setImageUrl(publicUrlResolver.toPublicUrl(vo.getImageUrl(), request));

        Result<UserUploadVO> result = Result.success(vo);
        result.setMessage("上传成功，等待审核");
        return result;
    }

    @GetMapping("/users/{username}/uploads")
    public Result<ItemsPageVO<UserUploadVO>> pageUserUploads(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page min is 1") Long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size min is 1") Long size,
            HttpServletRequest request) {
        User user = requireSelfUser(username);

        long safePage = Math.max(page, 1);
        long safeSize = normalizeSize(size);
        long offset = (safePage - 1) * safeSize;

        long total = uploadMapper.selectCount(new LambdaQueryWrapper<UserArtifactUploadEntity>()
                .eq(UserArtifactUploadEntity::getUserId, user.getObjectId()));

        List<UserUploadVO> items = uploadMapper.selectUserUploads(user.getObjectId(), offset, safeSize);
        items.forEach(item -> item.setImageUrl(publicUrlResolver.toPublicUrl(item.getImageUrl(), request)));

        return Result.success(new ItemsPageVO<>(total, safePage, safeSize, items));
    }

    @GetMapping("/users/{username}/uploads/{uploadId}")
    public Result<UserUploadVO> getUserUploadDetail(@PathVariable String username,
                                                   @PathVariable String uploadId,
                                                   HttpServletRequest request) {
        User user = requireSelfUser(username);

        UserUploadVO vo = uploadMapper.selectUserUploadDetail(user.getObjectId(), uploadId);

        if (vo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "上传记录不存在");
        }

        vo.setImageUrl(publicUrlResolver.toPublicUrl(vo.getImageUrl(), request));

        return Result.success(vo);
    }

    private void ensureArtifactExists(String artifactId) {
        if (artifactMapper.selectById(artifactId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文物不存在");
        }
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
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看自己的上传记录");
        }

        return user;
    }

    private long normalizeSize(long size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 100);
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
}
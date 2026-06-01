package com.platform.admin.modules.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.modules.artifact.mapper.ArtifactMapper;
import com.platform.admin.modules.audit.entity.ContentAudit;
import com.platform.admin.modules.audit.mapper.ContentAuditMapper;
import com.platform.admin.modules.interaction.dto.BrowseHistoryCreateRequest;
import com.platform.admin.modules.interaction.dto.CommentCreateRequest;
import com.platform.admin.modules.interaction.entity.ArtifactLikeEntity;
import com.platform.admin.modules.interaction.entity.CommentLikeEntity;
import com.platform.admin.modules.interaction.entity.UgcCommentEntity;
import com.platform.admin.modules.interaction.entity.UserBrowseHistoryEntity;
import com.platform.admin.modules.interaction.entity.UserFavoriteEntity;
import com.platform.admin.modules.interaction.mapper.ArtifactLikeMapper;
import com.platform.admin.modules.interaction.mapper.CommentLikeMapper;
import com.platform.admin.modules.interaction.mapper.UgcCommentMapper;
import com.platform.admin.modules.interaction.mapper.UserBrowseHistoryMapper;
import com.platform.admin.modules.interaction.mapper.UserFavoriteMapper;
import com.platform.admin.modules.interaction.service.InteractionService;
import com.platform.admin.modules.interaction.vo.CommentCreateVO;
import com.platform.admin.modules.interaction.vo.CommentVO;
import com.platform.admin.modules.interaction.vo.ItemsPageVO;
import com.platform.admin.modules.interaction.vo.LikeVO;
import com.platform.admin.modules.interaction.vo.UserBrowseHistoryVO;
import com.platform.admin.modules.interaction.vo.UserCommentVO;
import com.platform.admin.modules.interaction.vo.UserFavoriteVO;
import com.platform.admin.modules.interaction.vo.UserLikeVO;
import com.platform.admin.modules.system.service.SensitiveWordService;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.SecurityUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platform.admin.modules.interaction.dto.FavoriteCreateRequest;
import com.platform.admin.modules.interaction.dto.FavoriteGroupCreateRequest;
import com.platform.admin.modules.interaction.dto.FavoriteGroupUpdateRequest;
import com.platform.admin.modules.interaction.entity.UserFavoriteGroupEntity;
import com.platform.admin.modules.interaction.mapper.UserFavoriteGroupMapper;
import com.platform.admin.modules.interaction.vo.FavoriteActionVO;
import com.platform.admin.modules.interaction.vo.FavoriteGroupSummaryVO;
import com.platform.admin.modules.interaction.vo.FavoriteGroupVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InteractionServiceImpl implements InteractionService {
    private static final String APPROVED = "APPROVED";
    private static final String PENDING = "PENDING";

    private final UgcCommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final ArtifactLikeMapper artifactLikeMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserFavoriteGroupMapper userFavoriteGroupMapper;
    private final UserBrowseHistoryMapper browseHistoryMapper;
    private final ArtifactMapper artifactMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;
    private final ContentAuditMapper contentAuditMapper;
    private final SecurityUtil securityUtil;

    public InteractionServiceImpl(UgcCommentMapper commentMapper,
                                CommentLikeMapper commentLikeMapper,
                                ArtifactLikeMapper artifactLikeMapper,
                                UserFavoriteMapper userFavoriteMapper,
                                UserFavoriteGroupMapper userFavoriteGroupMapper,
                                UserBrowseHistoryMapper browseHistoryMapper,
                                ArtifactMapper artifactMapper,
                                UserMapper userMapper,
                                SensitiveWordService sensitiveWordService,
                                ContentAuditMapper contentAuditMapper,
                                SecurityUtil securityUtil) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.artifactLikeMapper = artifactLikeMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.userFavoriteGroupMapper = userFavoriteGroupMapper;
        this.browseHistoryMapper = browseHistoryMapper;
        this.artifactMapper = artifactMapper;
        this.userMapper = userMapper;
        this.sensitiveWordService = sensitiveWordService;
        this.contentAuditMapper = contentAuditMapper;
        this.securityUtil = securityUtil;
    }

    @Override
    public ItemsPageVO<CommentVO> pageArtifactComments(String artifactId, long page, long size) {
        ensureArtifactExists(artifactId);
        long safePage = Math.max(page, 1);
        long safeSize = normalizeSize(size);
        long offset = (safePage - 1) * safeSize;
        List<CommentVO> comments = commentMapper.selectTopLevelComments(artifactId, offset, safeSize);
        comments.forEach(item -> item.setReplies(commentMapper.selectReplies(item.getObjectId())));
        return new ItemsPageVO<>(commentMapper.countTopLevelComments(artifactId), safePage, safeSize, comments);
    }

    @Override
    @Transactional
    public CommentCreateVO createComment(String artifactId, CommentCreateRequest request) {
        ensureArtifactExists(artifactId);
        AuthUser currentUser = securityUtil.getCurrentUser();
        if (request.getParentId() != null && !request.getParentId().isBlank()) {
            UgcCommentEntity parent = commentMapper.selectById(request.getParentId());
            if (parent == null || !artifactId.equals(parent.getArtifactId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "父评论不存在");
            }
        }

        boolean sensitive = containsSensitiveWord(request.getContent());
        String status = sensitive ? PENDING : APPROVED;
        UgcCommentEntity comment = new UgcCommentEntity();
        comment.setObjectId(UUID.randomUUID().toString());
        comment.setArtifactId(artifactId);
        comment.setUserId(currentUser.objectId());
        comment.setParentId(blankToNull(request.getParentId()));
        comment.setContentText(request.getContent());
        comment.setStatus(status);
        comment.setLikes(0);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        insertAudit(comment, sensitive);
        String message = sensitive ? "评论已提交，等待审核" : "评论已发布";
        return new CommentCreateVO(comment.getObjectId(), status, message);
    }

    @Override
    public ItemsPageVO<UserCommentVO> pageUserComments(String username, long page, long size) {
        User user = requirePathUser(username);
        long safePage = Math.max(page, 1);
        long safeSize = normalizeSize(size);
        long offset = (safePage - 1) * safeSize;
        long total = commentMapper.selectCount(new LambdaQueryWrapper<UgcCommentEntity>()
                .eq(UgcCommentEntity::getUserId, user.getObjectId()));
        return new ItemsPageVO<>(total, safePage, safeSize,
                commentMapper.selectUserComments(user.getObjectId(), offset, safeSize));
    }

    @Override
    @Transactional
    public LikeVO likeComment(String commentId) {
        UgcCommentEntity comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        AuthUser currentUser = securityUtil.getCurrentUser();
        if (!existsCommentLike(commentId, currentUser.objectId())) {
            CommentLikeEntity like = new CommentLikeEntity();
            like.setObjectId(UUID.randomUUID().toString());
            like.setCommentId(commentId);
            like.setUserId(currentUser.objectId());
            like.setCreateTime(LocalDateTime.now());
            try {
                commentLikeMapper.insert(like);
                comment.setLikes((comment.getLikes() == null ? 0 : comment.getLikes()) + 1);
                commentMapper.updateById(comment);
            } catch (DuplicateKeyException ignored) {
                // 并发重复点赞时保持幂等。
            }
        }
        return new LikeVO(countCommentLikes(commentId), true);
    }

    @Override
    @Transactional
    public LikeVO likeArtifact(String artifactId) {
        ensureArtifactExists(artifactId);
        AuthUser currentUser = securityUtil.getCurrentUser();
        if (!existsArtifactLike(artifactId, currentUser.objectId())) {
            ArtifactLikeEntity like = new ArtifactLikeEntity();
            like.setObjectId(UUID.randomUUID().toString());
            like.setArtifactId(artifactId);
            like.setUserId(currentUser.objectId());
            like.setCreateTime(LocalDateTime.now());
            try {
                artifactLikeMapper.insert(like);
            } catch (DuplicateKeyException ignored) {
                // 并发重复点赞时保持幂等。
            }
        }
        return new LikeVO(countArtifactLikes(artifactId), true);
    }

    @Override
    @Transactional
    public LikeVO unlikeArtifact(String artifactId) {
        ensureArtifactExists(artifactId);
        AuthUser currentUser = securityUtil.getCurrentUser();
        artifactLikeMapper.delete(new LambdaQueryWrapper<ArtifactLikeEntity>()
                .eq(ArtifactLikeEntity::getArtifactId, artifactId)
                .eq(ArtifactLikeEntity::getUserId, currentUser.objectId()));
        return new LikeVO(countArtifactLikes(artifactId), false);
    }

    @Override
    public ItemsPageVO<UserLikeVO> pageUserLikes(String username, long page, long size) {
        User user = requirePathUser(username);
        long safePage = Math.max(page, 1);
        long safeSize = normalizeSize(size);
        long offset = (safePage - 1) * safeSize;
        long total = artifactLikeMapper.selectCount(new LambdaQueryWrapper<ArtifactLikeEntity>()
                .eq(ArtifactLikeEntity::getUserId, user.getObjectId()));
        return new ItemsPageVO<>(total, safePage, safeSize,
                artifactLikeMapper.selectUserLikes(user.getObjectId(), offset, safeSize));
    }

    @Override
    public ItemsPageVO<UserFavoriteVO> pageUserFavorites(String username, long page, long size) {
        User user = requirePathUser(username);
        long safePage = Math.max(page, 1);
        long safeSize = normalizeSize(size);
        long offset = (safePage - 1) * safeSize;
        long total = userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavoriteEntity>()
                .eq(UserFavoriteEntity::getUserId, user.getObjectId()));
        return new ItemsPageVO<>(total, safePage, safeSize,
                userFavoriteMapper.selectUserFavorites(user.getObjectId(), offset, safeSize));
    }

    @Override
public List<FavoriteGroupVO> listFavoriteGroups(String username) {
    User user = requirePathUser(username);
    ensureDefaultFavoriteGroup(user.getObjectId());
    return userFavoriteGroupMapper.selectGroups(user.getObjectId());
}

@Override
@Transactional
public FavoriteGroupVO createFavoriteGroup(String username, FavoriteGroupCreateRequest request) {
    User user = requirePathUser(username);
    String groupName = normalizeGroupName(request.getGroupName());

    UserFavoriteGroupEntity group = new UserFavoriteGroupEntity();
    group.setObjectId(UUID.randomUUID().toString());
    group.setUserId(user.getObjectId());
    group.setGroupName(groupName);
    group.setCreateTime(LocalDateTime.now());

    try {
        userFavoriteGroupMapper.insert(group);
    } catch (DuplicateKeyException ignored) {
        // 重复创建时保持幂等。
    }

    FavoriteGroupVO vo = new FavoriteGroupVO();
    vo.setGroupName(groupName);
    vo.setCreateTime(group.getCreateTime());
    return vo;
}

@Override
@Transactional
public FavoriteActionVO deleteFavoriteGroup(String username, String groupName) {
    User user = requirePathUser(username);
    String normalized = normalizeGroupName(groupName);

    if ("default".equals(normalized)) {
        throw new BusinessException(ErrorCode.BAD_REQUEST, "默认收藏夹不能删除");
    }

    userFavoriteMapper.delete(new LambdaQueryWrapper<UserFavoriteEntity>()
            .eq(UserFavoriteEntity::getUserId, user.getObjectId())
            .eq(UserFavoriteEntity::getGroupName, normalized));

    userFavoriteGroupMapper.delete(new LambdaQueryWrapper<UserFavoriteGroupEntity>()
            .eq(UserFavoriteGroupEntity::getUserId, user.getObjectId())
            .eq(UserFavoriteGroupEntity::getGroupName, normalized));

    return new FavoriteActionVO(null, normalized, false);
}

@Override
public List<FavoriteGroupSummaryVO> listFavoriteGroupSummary(String username) {
    User user = requirePathUser(username);
    ensureDefaultFavoriteGroup(user.getObjectId());
    return userFavoriteGroupMapper.selectGroupSummary(user.getObjectId());
}

@Override
@Transactional
public FavoriteActionVO addFavorite(String username, FavoriteCreateRequest request) {
    User user = requirePathUser(username);
    ensureArtifactExists(request.getArtifactId());

    String groupName = normalizeGroupName(request.getGroupName());
    ensureFavoriteGroup(user.getObjectId(), groupName);

    UserFavoriteEntity old = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavoriteEntity>()
            .eq(UserFavoriteEntity::getUserId, user.getObjectId())
            .eq(UserFavoriteEntity::getArtifactId, request.getArtifactId()));

    if (old != null) {
        old.setGroupName(groupName);
        userFavoriteMapper.updateById(old);
        return new FavoriteActionVO(request.getArtifactId(), groupName, true);
    }

    UserFavoriteEntity favorite = new UserFavoriteEntity();
    favorite.setObjectId(UUID.randomUUID().toString());
    favorite.setUserId(user.getObjectId());
    favorite.setArtifactId(request.getArtifactId());
    favorite.setGroupName(groupName);
    favorite.setCreateTime(LocalDateTime.now());

    try {
        userFavoriteMapper.insert(favorite);
    } catch (DuplicateKeyException ignored) {
        // 并发重复收藏时保持幂等。
    }

    return new FavoriteActionVO(request.getArtifactId(), groupName, true);
}

@Override
@Transactional
public FavoriteActionVO updateFavoriteGroup(String username, String artifactId, FavoriteGroupUpdateRequest request) {
    User user = requirePathUser(username);
    ensureArtifactExists(artifactId);

    String groupName = normalizeGroupName(request.getGroupName());
    ensureFavoriteGroup(user.getObjectId(), groupName);

    UserFavoriteEntity favorite = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavoriteEntity>()
            .eq(UserFavoriteEntity::getUserId, user.getObjectId())
            .eq(UserFavoriteEntity::getArtifactId, artifactId));

    if (favorite == null) {
        throw new BusinessException(ErrorCode.NOT_FOUND, "收藏记录不存在");
    }

    favorite.setGroupName(groupName);
    userFavoriteMapper.updateById(favorite);

    return new FavoriteActionVO(artifactId, groupName, true);
}

@Override
@Transactional
public FavoriteActionVO deleteFavorite(String username, String artifactId) {
    User user = requirePathUser(username);

    userFavoriteMapper.delete(new LambdaQueryWrapper<UserFavoriteEntity>()
            .eq(UserFavoriteEntity::getUserId, user.getObjectId())
            .eq(UserFavoriteEntity::getArtifactId, artifactId));

    return new FavoriteActionVO(artifactId, null, false);
}

    @Override
    public ItemsPageVO<UserBrowseHistoryVO> pageUserHistory(String username, long page, long size) {
        User user = requirePathUser(username);
        long safePage = Math.max(page, 1);
        long safeSize = normalizeSize(size);
        long offset = (safePage - 1) * safeSize;
        long total = browseHistoryMapper.selectCount(new LambdaQueryWrapper<UserBrowseHistoryEntity>()
                .eq(UserBrowseHistoryEntity::getUserId, user.getObjectId()));
        return new ItemsPageVO<>(total, safePage, safeSize,
                browseHistoryMapper.selectUserHistory(user.getObjectId(), offset, safeSize));
    }

    @Override
    @Transactional
    public UserBrowseHistoryVO addUserHistory(String username, BrowseHistoryCreateRequest request) {
        User user = requirePathUser(username);
        ensureArtifactExists(request.getArtifactId());
        UserBrowseHistoryEntity history = new UserBrowseHistoryEntity();
        history.setObjectId(UUID.randomUUID().toString());
        history.setUserId(user.getObjectId());
        history.setArtifactId(request.getArtifactId());
        history.setBrowseTime(LocalDateTime.now());
        browseHistoryMapper.insert(history);
        return browseHistoryMapper.selectUserHistory(user.getObjectId(), 0, 1).stream()
                .filter(item -> history.getObjectId().equals(item.getObjectId()))
                .findFirst()
                .orElseGet(() -> {
                    UserBrowseHistoryVO vo = new UserBrowseHistoryVO();
                    vo.setObjectId(history.getObjectId());
                    vo.setArtifactId(history.getArtifactId());
                    vo.setBrowseTime(history.getBrowseTime());
                    return vo;
                });
    }

    private void ensureArtifactExists(String artifactId) {
        if (artifactMapper.selectById(artifactId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文物不存在");
        }
    }

    private User requirePathUser(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0));
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        AuthUser currentUser = securityUtil.getCurrentUser();
        if (!user.getObjectId().equals(currentUser.objectId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看自己的用户数据");
        }
        return user;
    }

    private boolean containsSensitiveWord(String content) {
        return sensitiveWordService.getAllWords().stream()
                .anyMatch(word -> word.getWord() != null && !word.getWord().isBlank() && content.contains(word.getWord()));
    }

    private void insertAudit(UgcCommentEntity comment, boolean sensitive) {
        ContentAudit audit = new ContentAudit();
        audit.setObjectId(UUID.randomUUID().toString());
        audit.setContentType("COMMENT");
        audit.setContentText(comment.getContentText());
        audit.setAuthorId(comment.getUserId());
        audit.setAutoAuditResult(sensitive ? "MANUAL" : "PASS");
        audit.setAutoAuditDetail(sensitive ? "检测到敏感词，转人工审核" : "自动审核通过");
        audit.setStatus(comment.getStatus());
        audit.setSubmitTime(LocalDateTime.now());
        contentAuditMapper.insert(audit);
    }

    private long countCommentLikes(String commentId) {
        return commentLikeMapper.selectCount(new LambdaQueryWrapper<CommentLikeEntity>()
                .eq(CommentLikeEntity::getCommentId, commentId));
    }

    private long countArtifactLikes(String artifactId) {
        return artifactLikeMapper.selectCount(new LambdaQueryWrapper<ArtifactLikeEntity>()
                .eq(ArtifactLikeEntity::getArtifactId, artifactId));
    }

    private boolean existsCommentLike(String commentId, String userId) {
        return commentLikeMapper.selectCount(new LambdaQueryWrapper<CommentLikeEntity>()
                .eq(CommentLikeEntity::getCommentId, commentId)
                .eq(CommentLikeEntity::getUserId, userId)) > 0;
    }

    private boolean existsArtifactLike(String artifactId, String userId) {
        return artifactLikeMapper.selectCount(new LambdaQueryWrapper<ArtifactLikeEntity>()
                .eq(ArtifactLikeEntity::getArtifactId, artifactId)
                .eq(ArtifactLikeEntity::getUserId, userId)) > 0;
    }

    private long normalizeSize(long size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void ensureDefaultFavoriteGroup(String userId) {
    ensureFavoriteGroup(userId, "default");
}

private void ensureFavoriteGroup(String userId, String groupName) {
    String normalized = normalizeGroupName(groupName);

    long count = userFavoriteGroupMapper.selectCount(new LambdaQueryWrapper<UserFavoriteGroupEntity>()
            .eq(UserFavoriteGroupEntity::getUserId, userId)
            .eq(UserFavoriteGroupEntity::getGroupName, normalized));

    if (count > 0) {
        return;
    }

    UserFavoriteGroupEntity group = new UserFavoriteGroupEntity();
    group.setObjectId(UUID.randomUUID().toString());
    group.setUserId(userId);
    group.setGroupName(normalized);
    group.setCreateTime(LocalDateTime.now());

    try {
        userFavoriteGroupMapper.insert(group);
    } catch (DuplicateKeyException ignored) {
        // 并发创建同名收藏夹时保持幂等。
    }
}

private String normalizeGroupName(String groupName) {
    if (groupName == null || groupName.isBlank()) {
        return "default";
    }
    return groupName.trim();
}

}

package com.platform.admin.modules.interaction.service;

import com.platform.admin.modules.interaction.dto.BrowseHistoryCreateRequest;
import com.platform.admin.modules.interaction.dto.CommentCreateRequest;
import com.platform.admin.modules.interaction.vo.CommentCreateVO;
import com.platform.admin.modules.interaction.vo.CommentVO;
import com.platform.admin.modules.interaction.vo.ItemsPageVO;
import com.platform.admin.modules.interaction.vo.LikeVO;
import com.platform.admin.modules.interaction.vo.UserBrowseHistoryVO;
import com.platform.admin.modules.interaction.vo.UserCommentVO;
import com.platform.admin.modules.interaction.vo.UserFavoriteVO;
import com.platform.admin.modules.interaction.vo.UserLikeVO;

public interface InteractionService {
    ItemsPageVO<CommentVO> pageArtifactComments(String artifactId, long page, long size);

    CommentCreateVO createComment(String artifactId, CommentCreateRequest request);

    ItemsPageVO<UserCommentVO> pageUserComments(String username, long page, long size);

    LikeVO likeComment(String commentId);

    LikeVO likeArtifact(String artifactId);

    LikeVO unlikeArtifact(String artifactId);

    ItemsPageVO<UserLikeVO> pageUserLikes(String username, long page, long size);

    ItemsPageVO<UserFavoriteVO> pageUserFavorites(String username, long page, long size);

    ItemsPageVO<UserBrowseHistoryVO> pageUserHistory(String username, long page, long size);

    UserBrowseHistoryVO addUserHistory(String username, BrowseHistoryCreateRequest request);
}

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

import com.platform.admin.modules.interaction.dto.FavoriteCreateRequest;
import com.platform.admin.modules.interaction.dto.FavoriteGroupCreateRequest;
import com.platform.admin.modules.interaction.dto.FavoriteGroupUpdateRequest;
import com.platform.admin.modules.interaction.vo.FavoriteActionVO;
import com.platform.admin.modules.interaction.vo.FavoriteGroupSummaryVO;
import com.platform.admin.modules.interaction.vo.FavoriteGroupVO;
import java.util.List;

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

    List<FavoriteGroupVO> listFavoriteGroups(String username);

    FavoriteGroupVO createFavoriteGroup(String username, FavoriteGroupCreateRequest request);

    FavoriteActionVO deleteFavoriteGroup(String username, String groupName);

    List<FavoriteGroupSummaryVO> listFavoriteGroupSummary(String username);

    FavoriteActionVO addFavorite(String username, FavoriteCreateRequest request);

    FavoriteActionVO updateFavoriteGroup(String username, String artifactId, FavoriteGroupUpdateRequest request);

    FavoriteActionVO deleteFavorite(String username, String artifactId);
}

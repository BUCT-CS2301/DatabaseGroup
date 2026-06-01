package com.platform.admin.modules.interaction.controller;

import com.platform.admin.common.Result;
import com.platform.admin.modules.interaction.dto.BrowseHistoryCreateRequest;
import com.platform.admin.modules.interaction.dto.CommentCreateRequest;
import com.platform.admin.modules.interaction.service.InteractionService;
import com.platform.admin.modules.interaction.vo.CommentCreateVO;
import com.platform.admin.modules.interaction.vo.CommentVO;
import com.platform.admin.modules.interaction.vo.ItemsPageVO;
import com.platform.admin.modules.interaction.vo.LikeVO;
import com.platform.admin.modules.interaction.vo.UserBrowseHistoryVO;
import com.platform.admin.modules.interaction.vo.UserCommentVO;
import com.platform.admin.modules.interaction.vo.UserFavoriteVO;
import com.platform.admin.modules.interaction.vo.UserLikeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class InteractionController {
    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @GetMapping("/artifacts/{artifactId}/comments")
    public Result<ItemsPageVO<CommentVO>> pageArtifactComments(
            @PathVariable String artifactId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size最小为1") Long size) {
        return Result.success(interactionService.pageArtifactComments(artifactId, page, size));
    }

    @PostMapping("/artifacts/{artifactId}/comments")
    public Result<CommentCreateVO> createArtifactComment(
            @PathVariable String artifactId,
            @RequestBody @Valid CommentCreateRequest request) {
        return Result.success(interactionService.createComment(artifactId, request));
    }

    @GetMapping("/users/{username}/comments")
    public Result<ItemsPageVO<UserCommentVO>> pageUserComments(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size最小为1") Long size) {
        return Result.success(interactionService.pageUserComments(username, page, size));
    }

    @PostMapping("/comments/{commentId}/likes")
    public Result<LikeVO> likeComment(@PathVariable String commentId) {
        return Result.success(interactionService.likeComment(commentId));
    }

    @PostMapping("/artifacts/{artifactId}/likes")
    public Result<LikeVO> likeArtifact(@PathVariable String artifactId) {
        return Result.success(interactionService.likeArtifact(artifactId));
    }

    @DeleteMapping("/artifacts/{artifactId}/likes")
    public Result<LikeVO> unlikeArtifact(@PathVariable String artifactId) {
        return Result.success(interactionService.unlikeArtifact(artifactId));
    }

    @GetMapping("/users/{username}/likes")
    public Result<ItemsPageVO<UserLikeVO>> pageUserLikes(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size最小为1") Long size) {
        return Result.success(interactionService.pageUserLikes(username, page, size));
    }

    @GetMapping("/users/{username}/favorites")
    public Result<ItemsPageVO<UserFavoriteVO>> pageUserFavorites(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size最小为1") Long size) {
        return Result.success(interactionService.pageUserFavorites(username, page, size));
    }

    @GetMapping("/users/{username}/history")
    public Result<ItemsPageVO<UserBrowseHistoryVO>> pageUserHistory(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size最小为1") Long size) {
        return Result.success(interactionService.pageUserHistory(username, page, size));
    }

    @PostMapping("/users/{username}/history")
    public Result<UserBrowseHistoryVO> addUserHistory(
            @PathVariable String username,
            @RequestBody @Valid BrowseHistoryCreateRequest request) {
        return Result.success(interactionService.addUserHistory(username, request));
    }
}

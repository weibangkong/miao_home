package com.miaohome.comment.controller;

import com.miaohome.comment.dto.CatCommentRequest;
import com.miaohome.comment.dto.CatCommentResponse;
import com.miaohome.comment.dto.LikeResultResponse;
import com.miaohome.comment.entity.CatLike;
import com.miaohome.comment.repository.CatLikeRepository;
import com.miaohome.comment.service.CatCommentService;
import com.miaohome.config.SessionContext;
import com.miaohome.dto.ApiResult;
import com.miaohome.repository.CatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 评论与点赞接口
 * 用户 ID 从 Session 获取，不再通过请求体传递。
 *
 * @author weibang kong
 */
@Tag(name = "评论与点赞", description = "猫咪点赞、评论发表/删除、评论点赞")
@RestController
public class CommentController {

    private final CatCommentService commentService;
    private final CatLikeRepository catLikeRepo;
    private final CatRepository catRepo;

    public CommentController(CatCommentService commentService,
                             CatLikeRepository catLikeRepo,
                             CatRepository catRepo) {
        this.commentService = commentService;
        this.catLikeRepo = catLikeRepo;
        this.catRepo = catRepo;
    }

    // ==================== 猫咪点赞 ====================

    /**
     * 给猫咪点赞 / 取消点赞
     */
    @Operation(summary = "给猫咪点赞/取消点赞")
    @PostMapping(value = "/cats/{catId}/like", consumes = "application/json")
    public ApiResult<LikeResultResponse> toggleCatLike(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId) {
        Long userId = SessionContext.getUserId();

        Optional<CatLike> existing = catLikeRepo.findByCatIdAndUserId(catId, userId);
        boolean liked;
        if (existing.isPresent()) {
            catLikeRepo.delete(existing.get());
            liked = false;
        } else {
            CatLike like = new CatLike();
            like.setCatId(catId);
            like.setUserId(userId);
            catLikeRepo.save(like);
            liked = true;
        }

        // 更新冗余字段
        long likeCount = catLikeRepo.countByCatId(catId);
        catRepo.findById(catId).ifPresent(cat -> {
            cat.setLikeCount((int) likeCount);
            catRepo.save(cat);
        });

        LikeResultResponse result = new LikeResultResponse();
        result.setLiked(liked);
        result.setLikeCount(likeCount);
        return ApiResult.success(result);
    }

    /**
     * 查询当前用户是否已点赞某猫咪（需登录）
     */
    @Operation(summary = "查询当前用户是否已点赞某猫咪")
    @GetMapping("/cats/{catId}/like/status")
    public ApiResult<LikeResultResponse> getCatLikeStatus(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId) {
        if (!SessionContext.isLoggedIn()) {
            LikeResultResponse result = new LikeResultResponse();
            result.setLiked(false);
            return ApiResult.success(result);
        }
        Long userId = SessionContext.getUserId();
        boolean liked = catLikeRepo.findByCatIdAndUserId(catId, userId).isPresent();
        LikeResultResponse result = new LikeResultResponse();
        result.setLiked(liked);
        return ApiResult.success(result);
    }

    // ==================== 评论 ====================

    /**
     * 获取猫咪评论列表
     */
    @Operation(summary = "获取猫咪评论列表")
    @GetMapping("/cats/{catId}/comments/list")
    public ApiResult<List<CatCommentResponse>> listComments(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "created_at") String sortBy,
            @Parameter(description = "排序方向（asc/desc）") @RequestParam(defaultValue = "desc") String order) {
        Long currentUserId = SessionContext.isLoggedIn() ? SessionContext.getUserId() : null;
        List<CatCommentResponse> comments = commentService.getComments(catId, sortBy, order, currentUserId);
        return ApiResult.success(comments);
    }

    /**
     * 发表评论或回复（需登录）
     */
    @Operation(summary = "发表评论或回复")
    @PostMapping(value = "/cats/{catId}/comments", consumes = "application/json")
    public ApiResult<CatCommentResponse> createComment(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId,
            @Parameter(description = "评论内容") @RequestBody CatCommentRequest request) {
        CatCommentResponse comment = commentService.createComment(catId, request);
        return ApiResult.success(comment);
    }

    /**
     * 删除评论（仅评论创建者可删除，需登录）
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{commentId}")
    public ApiResult<Void> deleteComment(@Parameter(description = "评论 ID") @PathVariable Long commentId) {
        Long userId = SessionContext.getUserId();
        commentService.deleteComment(commentId, userId);
        return ApiResult.success(null);
    }

    /**
     * 评论点赞 / 取消点赞（需登录）
     */
    @Operation(summary = "评论点赞/取消点赞")
    @PostMapping(value = "/comments/{commentId}/like", consumes = "application/json")
    public ApiResult<LikeResultResponse> toggleCommentLike(
            @Parameter(description = "评论 ID") @PathVariable Long commentId) {
        Long userId = SessionContext.getUserId();
        LikeResultResponse result = commentService.toggleLike(commentId, userId);
        return ApiResult.success(result);
    }
}

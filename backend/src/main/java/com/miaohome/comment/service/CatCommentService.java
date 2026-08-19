package com.miaohome.comment.service;

import com.miaohome.comment.dto.CatCommentRequest;
import com.miaohome.comment.dto.CatCommentResponse;
import com.miaohome.comment.dto.LikeResultResponse;
import com.miaohome.comment.entity.CatComment;
import com.miaohome.comment.entity.CatCommentLike;
import com.miaohome.comment.repository.CatCommentLikeRepository;
import com.miaohome.comment.repository.CatCommentRepository;
import com.miaohome.config.SessionContext;
import com.miaohome.entity.User;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.repository.UserRepository;
import com.miaohome.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 猫咪评论业务逻辑
 * 支持嵌套回复、点赞切换、多维度排序。
 *
 * @author weibang kong
 */
@Service
@Transactional
public class CatCommentService {

    private final CatCommentRepository commentRepo;
    private final CatCommentLikeRepository commentLikeRepo;
    private final UserRepository userRepo;

    public CatCommentService(CatCommentRepository commentRepo,
                             CatCommentLikeRepository commentLikeRepo,
                             UserRepository userRepo) {
        this.commentRepo = commentRepo;
        this.commentLikeRepo = commentLikeRepo;
        this.userRepo = userRepo;
    }

    /**
     * 获取猫咪评论列表（含嵌套回复），支持排序
     *
     * @param catId         猫咪 ID
     * @param sortBy        排序字段：created_at / like_count
     * @param order         排序方向：asc / desc
     * @param currentUserId 当前用户 ID（可为 null，用于判断是否已点赞）
     */
    public List<CatCommentResponse> getComments(Long catId, String sortBy, String order, Long currentUserId) {
        List<CatComment> topLevel;
        boolean asc = "asc".equalsIgnoreCase(order);

        if ("like_count".equals(sortBy)) {
            topLevel = commentRepo.findTopLevelByCatIdOrderByLikeCountDesc(catId);
        } else {
            topLevel = commentRepo.findTopLevelByCatIdOrderByCreatedAtDesc(catId);
        }

        if (asc) {
            Collections.reverse(topLevel);
        }

        // 收集所有评论 ID（顶级 + 回复），批量查询用户信息和点赞状态
        List<CatComment> allComments = new ArrayList<>(topLevel);
        for (CatComment parent : topLevel) {
            List<CatComment> replies = commentRepo.findByParentIdOrderByCreatedAtAsc(parent.getId());
            allComments.addAll(replies);
        }

        List<Long> allCommentIds = allComments.stream().map(CatComment::getId).collect(Collectors.toList());
        Set<Long> userIds = allComments.stream().map(CatComment::getUserId).collect(Collectors.toSet());

        // 批量查询用户
        Map<Long, User> userMap = userRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查询当前用户的点赞状态
        Set<Long> likedCommentIds = Collections.emptySet();
        if (currentUserId != null && !allCommentIds.isEmpty()) {
            likedCommentIds = commentLikeRepo.findByCommentIdInAndUserId(allCommentIds, currentUserId)
                    .stream().map(CatCommentLike::getCommentId).collect(Collectors.toSet());
        }

        // 构建回复 Map
        Map<Long, List<CatComment>> replyMap = new HashMap<>();
        for (CatComment c : allComments) {
            if (c.getParentId() != null) {
                replyMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
            }
        }

        final Set<Long> finalLikedIds = likedCommentIds;
        return topLevel.stream().map(c -> toResponse(c, userMap, finalLikedIds, replyMap)).collect(Collectors.toList());
    }

    /**
     * 创建评论或回复，用户 ID 从 Session 获取
     */
    public CatCommentResponse createComment(Long catId, CatCommentRequest request) {
        Long userId = SessionContext.getUserId();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        CatComment comment = new CatComment();
        comment.setCatId(catId);
        comment.setTenantId(TenantContext.getTenantId());
        comment.setUserId(user.getId());
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        comment = commentRepo.save(comment);

        CatCommentResponse resp = toFlatResponse(comment);
        resp.setNickname(user.getNickname());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setLikedByCurrentUser(false);
        return resp;
    }

    /**
     * 删除评论（仅评论创建者可删除）
     */
    public void deleteComment(Long commentId, Long userId) {
        CatComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "评论不存在"));
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除他人评论");
        }
        // 级联删除由数据库 ON DELETE CASCADE 处理子回复和点赞
        commentRepo.delete(comment);
    }

    /**
     * 点赞 / 取消点赞切换
     */
    public LikeResultResponse toggleLike(Long commentId, Long userId) {
        CatComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "评论不存在"));

        Optional<CatCommentLike> existing = commentLikeRepo.findByCommentIdAndUserId(commentId, userId);
        boolean liked;
        if (existing.isPresent()) {
            commentLikeRepo.delete(existing.get());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            liked = false;
        } else {
            CatCommentLike like = new CatCommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeRepo.save(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            liked = true;
        }
        commentRepo.save(comment);

        LikeResultResponse result = new LikeResultResponse();
        result.setLiked(liked);
        result.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount().longValue() : 0L);
        return result;
    }

    /**
     * 删除猫咪下所有评论（级联）
     */
    public void deleteAllByCatId(Long catId) {
        commentRepo.deleteByCatId(catId);
    }

    // ---- 内部方法 ----

    private CatCommentResponse toResponse(CatComment comment, Map<Long, User> userMap,
                                          Set<Long> likedIds, Map<Long, List<CatComment>> replyMap) {
        CatCommentResponse resp = toFlatResponse(comment);
        User user = userMap.get(comment.getUserId());
        resp.setNickname(user != null ? user.getNickname() : "未知用户");
        resp.setAvatarUrl(user != null ? user.getAvatarUrl() : null);
        resp.setLikedByCurrentUser(likedIds.contains(comment.getId()));

        List<CatComment> replies = replyMap.get(comment.getId());
        if (replies != null && !replies.isEmpty()) {
            resp.setReplies(replies.stream().map(r -> toResponse(r, userMap, likedIds, replyMap))
                    .collect(Collectors.toList()));
        } else {
            resp.setReplies(Collections.emptyList());
        }
        return resp;
    }

    private CatCommentResponse toFlatResponse(CatComment comment) {
        CatCommentResponse resp = new CatCommentResponse();
        resp.setId(comment.getId());
        resp.setCatId(comment.getCatId());
        resp.setTenantId(comment.getTenantId());
        resp.setUserId(comment.getUserId());
        resp.setParentId(comment.getParentId());
        resp.setContent(comment.getContent());
        resp.setLikeCount(comment.getLikeCount());
        resp.setCreatedAt(comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null);
        resp.setUpdatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt().toString() : null);
        return resp;
    }
}

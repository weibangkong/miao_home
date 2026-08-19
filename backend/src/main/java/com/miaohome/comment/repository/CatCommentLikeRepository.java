package com.miaohome.comment.repository;

import com.miaohome.comment.entity.CatCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatCommentLikeRepository extends JpaRepository<CatCommentLike, Long> {

    /** 查询用户是否已点赞某条评论 */
    Optional<CatCommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    /** 查询用户对多条评论的点赞状态 */
    List<CatCommentLike> findByCommentIdInAndUserId(List<Long> commentIds, Long userId);

    /** 统计评论点赞数 */
    long countByCommentId(Long commentId);
}

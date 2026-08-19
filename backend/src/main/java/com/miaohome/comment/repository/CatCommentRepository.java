package com.miaohome.comment.repository;

import com.miaohome.comment.entity.CatComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatCommentRepository extends JpaRepository<CatComment, Long> {

    /** 获取猫咪的顶级评论，按创建时间排序 */
    @Query("SELECT c FROM CatComment c WHERE c.catId = :catId AND c.parentId IS NULL ORDER BY c.createdAt DESC")
    List<CatComment> findTopLevelByCatIdOrderByCreatedAtDesc(@Param("catId") Long catId);

    /** 获取猫咪的顶级评论，按点赞数排序 */
    @Query("SELECT c FROM CatComment c WHERE c.catId = :catId AND c.parentId IS NULL ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<CatComment> findTopLevelByCatIdOrderByLikeCountDesc(@Param("catId") Long catId);

    /** 获取某条评论的所有子回复，按时间正序 */
    List<CatComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    /** 删除猫咪下所有评论 */
    void deleteByCatId(Long catId);
}

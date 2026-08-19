package com.miaohome.comment.repository;

import com.miaohome.comment.entity.CatLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatLikeRepository extends JpaRepository<CatLike, Long> {

    Optional<CatLike> findByCatIdAndUserId(Long catId, Long userId);

    List<CatLike> findByUserId(Long userId);

    long countByCatId(Long catId);
}

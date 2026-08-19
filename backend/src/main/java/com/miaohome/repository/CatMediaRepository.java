package com.miaohome.repository;

import com.miaohome.entity.CatMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatMediaRepository extends JpaRepository<CatMedia, Long> {
    List<CatMedia> findByCatIdOrderByCreatedAtDesc(Long catId);
    List<CatMedia> findByCatIdAndMediaTypeOrderByCreatedAtDesc(Long catId, CatMedia.MediaType mediaType);
    void deleteByCatId(Long catId);
}

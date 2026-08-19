package com.miaohome.repository;

import com.miaohome.entity.CatHealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatHealthRecordRepository extends JpaRepository<CatHealthRecord, Long> {
    List<CatHealthRecord> findByCatIdOrderByCreatedAtDesc(Long catId);
    void deleteByCatId(Long catId);
}

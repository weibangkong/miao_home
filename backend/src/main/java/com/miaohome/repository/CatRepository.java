package com.miaohome.repository;

import com.miaohome.entity.Cat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatRepository extends JpaRepository<Cat, Long> {
    List<Cat> findAllByOrderByCreatedAtDesc();
    List<Cat> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<Cat> findByTenantIdAndIsAdopted(Long tenantId, Boolean isAdopted);
    List<Cat> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name);
}

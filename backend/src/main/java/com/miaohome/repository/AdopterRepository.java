package com.miaohome.repository;

import com.miaohome.entity.Adopter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdopterRepository extends JpaRepository<Adopter, Long> {
    List<Adopter> findAllByOrderByAdoptedAtDesc();
    List<Adopter> findByTenantIdOrderByAdoptedAtDesc(Long tenantId);
    List<Adopter> findByCatId(Long catId);
    List<Adopter> findByBuildingOrderByAdoptedAtDesc(String building);
    List<Adopter> findByTenantIdAndHouseholdNumberContainingIgnoreCase(Long tenantId, String householdNumber);
    Optional<Adopter> findByCatIdAndIsActive(Long catId, Boolean isActive);
}

package com.miaohome.repository;

import com.miaohome.entity.CatLocation;
import com.miaohome.entity.CatLocationPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 猫咪出没地点数据访问
 */
@Repository
public interface CatLocationRepository extends JpaRepository<CatLocation, CatLocationPK> {

    /** 根据猫咪 ID 和所属小区 ID 删除记录 */
    void deleteByCatIdAndTenantId(Long catId, Long tenantId);
}

package com.miaohome.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 猫咪出没地点联合主键
 */
public class CatLocationPK implements Serializable {

    /** 关联猫咪 ID */
    private Long catId;

    /** 猫咪所属小区 ID */
    private Long tenantId;


    public CatLocationPK() {}

    public CatLocationPK(Long catId, Long tenantId) {
        this.catId = catId;
        this.tenantId = tenantId;
    }


    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatLocationPK that = (CatLocationPK) o;
        return Objects.equals(catId, that.catId) && Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catId, tenantId);
    }
}

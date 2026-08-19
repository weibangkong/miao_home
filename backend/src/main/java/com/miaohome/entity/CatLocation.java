package com.miaohome.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

/**
 * 猫咪出没地点
 * 记录猫咪在各小区的出现楼栋，以 JSONB 格式存储。
 */
@Entity
@Table(name = "mh_cat_location")
@IdClass(CatLocationPK.class)
public class CatLocation {

    /** 关联猫咪 ID（联合主键） */
    @Id
    @Column(name = "cat_id", nullable = false)
    private Long catId;

    /** 猫咪所属小区 ID（联合主键） */
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 出没楼栋列表，JSONB 格式：[{"tenantId":1,"building":"A1"}] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String building;

    /** 记录创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 记录最后更新时间 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();


    // ---- 默认构造 ----
    public CatLocation() {}


    // ---- getters / setters ----

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

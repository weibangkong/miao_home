package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 认养记录
 */
@Entity
@Table(name = "mh_adopter")
public class Adopter {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 认养的猫咪 ID */
    @Column(name = "cat_id", nullable = false)
    private Long catId;

    /** 所属租户 ID，用于多租户隔离 */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 认养操作人用户 ID，关联 mh_user.id */
    @Column(name = "user_id")
    private Long userId;

    /** 认养户号（如 A1-101，小区内唯一标识住户） */
    @Column(name = "household_number", nullable = false, length = 50)
    private String householdNumber;

    /** 认养人姓名（可选） */
    @Column(name = "adopter_name", length = 100)
    private String adopterName;

    /** 联系电话（可选） */
    @Column(length = 20)
    private String phone;

    /** 认养人所在楼栋 */
    @Column(length = 50)
    private String building;

    /** 认养人房间单元号 */
    @Column(name = "unit_number", length = 20)
    private String unitNumber;

    /** 认养时间 */
    @Column(name = "adopted_at")
    private LocalDateTime adoptedAt = LocalDateTime.now();

    /** 认养是否仍然有效（用于软删除，true=有效） */
    @Column(name = "is_active")
    private Boolean isActive = true;

    // ---- 默认构造 ----
    public Adopter() {}

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getHouseholdNumber() { return householdNumber; }
    public void setHouseholdNumber(String householdNumber) { this.householdNumber = householdNumber; }

    public String getAdopterName() { return adopterName; }
    public void setAdopterName(String adopterName) { this.adopterName = adopterName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }

    public LocalDateTime getAdoptedAt() { return adoptedAt; }
    public void setAdoptedAt(LocalDateTime adoptedAt) { this.adoptedAt = adoptedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

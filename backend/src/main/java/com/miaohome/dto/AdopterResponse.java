package com.miaohome.dto;

/**
 * 认养响应 DTO
 */
public class AdopterResponse {

    /** 认养记录 ID */
    private Long id;

    /** 认养的猫咪 ID */
    private Long catId;

    /** 所属租户 ID */
    private Long tenantId;

    /** 认养操作人用户 ID */
    private Long userId;

    /** 认养户号 */
    private String householdNumber;

    /** 认养人姓名 */
    private String adopterName;

    /** 联系电话 */
    private String phone;

    /** 认养人所在楼栋 */
    private String building;

    /** 房间单元号 */
    private String unitNumber;

    /** 认养时间（ISO 字符串） */
    private String adoptedAt;

    /** 认养是否有效 */
    private Boolean isActive;

    /** 认养的猫咪名称 */
    private String catName;

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

    public String getAdoptedAt() { return adoptedAt; }
    public void setAdoptedAt(String adoptedAt) { this.adoptedAt = adoptedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCatName() { return catName; }
    public void setCatName(String catName) { this.catName = catName; }
}

package com.miaohome.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 认养请求 DTO
 */
public class AdopterRequest {

    /** 认养户号（如 A1-101，必填） */
    @NotBlank(message = "认养户号不能为空")
    private String householdNumber;

    /** 认养人姓名（可选） */
    private String adopterName;

    /** 联系电话（可选） */
    private String phone;

    /** 认养人所在楼栋 */
    private String building;

    /** 认养人房间单元号 */
    private String unitNumber;

    // ---- getters / setters ----

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
}

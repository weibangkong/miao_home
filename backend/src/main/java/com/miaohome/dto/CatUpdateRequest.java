package com.miaohome.dto;

/**
 * 猫咪更新请求 DTO（含认养状态、绝育字段）
 */
public class CatUpdateRequest {

    /** 猫咪名称 */
    private String name;

    /** 毛色 */
    private String color;

    /** 性别：公/母/未知 */
    private String gender;

    /** 出生年份 */
    private Integer birthYear;

    /** 描述 */
    private String description;

    /** 是否已被认养 */
    private Boolean isAdopted;

    /** 是否已绝育 */
    private Boolean isNeutered;

    /** 所在楼栋 */
    private String building;

    // ---- getters / setters ----

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsAdopted() { return isAdopted; }
    public void setIsAdopted(Boolean isAdopted) { this.isAdopted = isAdopted; }

    public Boolean getIsNeutered() { return isNeutered; }
    public void setIsNeutered(Boolean isNeutered) { this.isNeutered = isNeutered; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
}

package com.miaohome.dto;

import java.util.List;

/**
 * 猫咪创建/更新请求 DTO
 */
public class CatCreateRequest {

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

    /** 是否已绝育 */
    private Boolean isNeutered;

    /** 经常出现小区列表 */
    private List<FrequentCommunityItem> frequentCommunities;

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

    public Boolean getIsNeutered() { return isNeutered; }
    public void setIsNeutered(Boolean isNeutered) { this.isNeutered = isNeutered; }

    public List<FrequentCommunityItem> getFrequentCommunities() { return frequentCommunities; }
    public void setFrequentCommunities(List<FrequentCommunityItem> frequentCommunities) { this.frequentCommunities = frequentCommunities; }
}

package com.miaohome.dto;

import java.util.List;

public class CatResponse {

    private Long id;
    private Long tenantId;
    private String name;
    private String color;
    private String gender;
    private Integer birthYear;
    private String description;
    private String avatarUrl;
    private Boolean isAdopted;
    private Boolean isNeutered;
    private Integer likeCount;
    private String createdAt;
    private String updatedAt;
    private List<CatMediaResponse> mediaList;
    private AdopterResponse currentAdopter;
    private List<CatHealthRecordResponse> healthRecordList;
    private List<FrequentCommunityItem> locationList;

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

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

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Boolean getIsAdopted() { return isAdopted; }
    public void setIsAdopted(Boolean isAdopted) { this.isAdopted = isAdopted; }

    public Boolean getIsNeutered() { return isNeutered; }
    public void setIsNeutered(Boolean isNeutered) { this.isNeutered = isNeutered; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<CatMediaResponse> getMediaList() { return mediaList; }
    public void setMediaList(List<CatMediaResponse> mediaList) { this.mediaList = mediaList; }

    public AdopterResponse getCurrentAdopter() { return currentAdopter; }
    public void setCurrentAdopter(AdopterResponse currentAdopter) { this.currentAdopter = currentAdopter; }

    public List<CatHealthRecordResponse> getHealthRecordList() { return healthRecordList; }
    public void setHealthRecordList(List<CatHealthRecordResponse> healthRecordList) { this.healthRecordList = healthRecordList; }

    public List<FrequentCommunityItem> getLocationList() { return locationList; }
    public void setLocationList(List<FrequentCommunityItem> locationList) { this.locationList = locationList; }

}

package com.miaohome.dto;

/**
 * 猫咪健康记录响应 DTO
 */
public class CatHealthRecordResponse {

    /** 健康记录 ID */
    private Long id;

    /** 关联猫咪 ID */
    private Long catId;

    /** 是否健康异常 */
    private Boolean isSick;

    /** 疾病名称 / 诊断 */
    private String diseaseName;

    /** 症状描述 */
    private String description;

    /** 治疗措施 */
    private String treatment;

    /** 记录日期 */
    private String recordDate;

    /** 创建时间 */
    private String createdAt;

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public Boolean getIsSick() { return isSick; }
    public void setIsSick(Boolean isSick) { this.isSick = isSick; }

    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

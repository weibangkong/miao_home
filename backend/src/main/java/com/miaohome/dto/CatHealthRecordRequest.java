package com.miaohome.dto;

/**
 * 猫咪健康记录请求 DTO
 */
public class CatHealthRecordRequest {

    /** 是否健康异常（true=生病/有恙，false=健康） */
    private Boolean isSick;

    /** 疾病名称 / 诊断结果 */
    private String diseaseName;

    /** 症状描述 */
    private String description;

    /** 治疗措施 */
    private String treatment;

    /** 记录日期（格式：yyyy-MM-dd） */
    private String recordDate;

    // ---- getters / setters ----

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
}

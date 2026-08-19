package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 猫咪健康记录
 * <p>记录猫咪的病史、健康检查等信息，与猫咪一对多关联。</p>
 */
@Entity
@Table(name = "mh_cat_health_record")
public class CatHealthRecord {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联猫咪 ID */
    @Column(name = "cat_id", nullable = false)
    private Long catId;

    /** 所属租户 ID */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 是否健康异常（true=生病/有恙，false=健康） */
    @Column(name = "is_sick")
    private Boolean isSick = true;

    /** 疾病名称 / 诊断结果 */
    @Column(name = "disease_name", length = 200)
    private String diseaseName;

    /** 症状描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 治疗措施 */
    @Column(columnDefinition = "TEXT")
    private String treatment;

    /** 记录日期 */
    @Column(name = "record_date")
    private LocalDate recordDate = LocalDate.now();

    /** 记录创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ---- 默认构造 ----
    public CatHealthRecord() {}

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Boolean getIsSick() { return isSick; }
    public void setIsSick(Boolean isSick) { this.isSick = isSick; }

    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

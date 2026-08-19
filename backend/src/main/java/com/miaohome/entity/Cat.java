package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 猫咪
 */
@Entity
@Table(name = "mh_cat")
public class Cat {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属租户（小区）ID，用于多租户隔离 */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 猫咪名称（可为空，如 "小花"、"橘座"） */
    @Column(length = 100)
    private String name;

    /** 毛色（如 "橘色"、"黑白相间"、"狸花"） */
    @Column(length = 50)
    private String color;

    /** 性别：公 / 母 / 未知 */
    @Column(length = 10)
    private String gender;

    /** 出生年份（用于估算年龄） */
    @Column(name = "birth_year")
    private Integer birthYear;

    /** 猫咪描述（特征、习性、健康状况等） */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 头像文件存储路径（相对路径） */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /** 是否已被认养 */
    @Column(name = "is_adopted")
    private Boolean isAdopted = false;

    /** 是否已绝育 */
    @Column(name = "is_neutered")
    private Boolean isNeutered = false;

    /** 点赞数（冗余字段） */
    @Column(name = "like_count")
    private Integer likeCount = 0;

    /** 记录创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 记录最后更新时间 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ---- 默认构造 ----
    public Cat() {}

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 通知消息
 */
@Entity
@Table(name = "mh_notification")
public class Notification {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属租户 ID，用于多租户隔离 */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 目标认养人 ID（可为 null，表示全局通知） */
    @Column(name = "adopter_id")
    private Long adopterId;

    /** 关联猫咪 ID（可为 null） */
    @Column(name = "cat_id")
    private Long catId;

    /** 通知标题（如 "猫咪疫苗接种提醒"） */
    @Column(nullable = false, length = 200)
    private String title;

    /** 通知正文 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 是否已读 */
    @Column(name = "is_read")
    private Boolean isRead = false;

    /** 记录创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ---- 默认构造 ----
    public Notification() {}

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getAdopterId() { return adopterId; }
    public void setAdopterId(Long adopterId) { this.adopterId = adopterId; }

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

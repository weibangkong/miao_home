package com.miaohome.dto;

/**
 * 通知响应 DTO
 */
public class NotificationResponse {

    /** 通知 ID */
    private Long id;

    /** 所属租户 ID */
    private Long tenantId;

    /** 目标认养人 ID（可为 null，表示全局通知） */
    private Long adopterId;

    /** 关联猫咪 ID（可为 null） */
    private Long catId;

    /** 通知标题 */
    private String title;

    /** 通知正文 */
    private String content;

    /** 是否已读 */
    private Boolean isRead;

    /** 创建时间（ISO 字符串） */
    private String createdAt;

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

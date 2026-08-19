package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 猫咪媒体（照片 / 视频）
 */
@Entity
@Table(name = "mh_cat_media")
public class CatMedia {

    /** 媒体类型枚举 */
    public enum MediaType { PHOTO, VIDEO }

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联猫咪 ID */
    @Column(name = "cat_id", nullable = false)
    private Long catId;

    /** 所属租户 ID，用于多租户隔离 */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 媒体类型：PHOTO（照片）/ VIDEO（视频） */
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    /** 文件存储相对路径（格式：public/yyyyMMdd/uuid.ext） */
    @Column(nullable = false, length = 500)
    private String url;

    /** 年龄阶段标记：幼猫 / 少年 / 成年 / 老年 */
    @Column(length = 50)
    private String ageStage;

    /** 是否为猫咪头像（每只猫咪最多一张为 true） */
    @Column(name = "is_avatar")
    private Boolean isAvatar = false;

    /** 上传时的原始文件名 */
    @Column(name = "file_name", length = 255)
    private String fileName;

    /** 文件大小，单位字节 */
    @Column(name = "file_size")
    private Long fileSize;

    /** 记录创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ---- 默认构造 ----
    public CatMedia() {}

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAgeStage() { return ageStage; }
    public void setAgeStage(String ageStage) { this.ageStage = ageStage; }

    public Boolean getIsAvatar() { return isAvatar; }
    public void setIsAvatar(Boolean isAvatar) { this.isAvatar = isAvatar; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

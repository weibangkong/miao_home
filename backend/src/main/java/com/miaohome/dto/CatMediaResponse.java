package com.miaohome.dto;

/**
 * 猫咪媒体（照片/视频）响应 DTO
 */
public class CatMediaResponse {

    /** 媒体记录 ID */
    private Long id;

    /** 关联猫咪 ID */
    private Long catId;

    /** 媒体类型：PHOTO / VIDEO */
    private String mediaType;

    /** 文件存储相对路径 */
    private String url;

    /** 年龄阶段：幼猫/少年/成年/老年 */
    private String ageStage;

    /** 是否为头像 */
    private Boolean isAvatar;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传时间（ISO 字符串） */
    private String createdAt;

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

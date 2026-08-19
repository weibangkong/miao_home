package com.miaohome.dto;

/**
 * 猫咪媒体直传回传请求 DTO
 * 前端直传 OSS 完成后，将对象键等信息回传给后端以落库。
 */
public class CatMediaObjectRequest {

    /** OSS 对象键（相对路径，如 public/yyyyMMdd/uuid.ext） */
    private String objectKey;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 媒体类型：PHOTO / VIDEO */
    private String mediaType;

    /** 年龄阶段标记 */
    private String ageStage;

    /** 是否设为头像 */
    private Boolean isAvatar;

    // ---- getters / setters ----

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getAgeStage() { return ageStage; }
    public void setAgeStage(String ageStage) { this.ageStage = ageStage; }

    public Boolean getIsAvatar() { return isAvatar; }
    public void setIsAvatar(Boolean isAvatar) { this.isAvatar = isAvatar; }
}

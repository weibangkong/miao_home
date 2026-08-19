package com.miaohome.dto;

/**
 * 用户头像直传回传请求 DTO
 * 前端直传 OSS 完成后，将对象键回传给后端以更新用户头像。
 */
public class UserAvatarRequest {

    /** OSS 对象键（相对路径，如 public/yyyyMMdd/uuid.ext） */
    private String objectKey;

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
}

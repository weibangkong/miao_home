package com.miaohome.dto;

/**
 * STS 临时凭证响应 DTO
 * 用于前端直传阿里云 OSS。
 */
public class StsCredentialsResponse {

    /** 临时访问密钥 ID */
    private String accessKeyId;

    /** 临时访问密钥 Secret */
    private String accessKeySecret;

    /** 安全令牌（SecurityToken） */
    private String securityToken;

    /** 凭证过期时间（ISO 8601 字符串） */
    private String expiration;

    /** OSS 地域，如 oss-cn-hangzhou */
    private String region;

    /** 存储桶名称 */
    private String bucket;

    /** OSS 服务端点 */
    private String endpoint;

    /** 允许上传的目录前缀，以 / 结尾，如 public/yyyyMMdd/ */
    private String dir;

    // ---- getters / setters ----

    public String getAccessKeyId() { return accessKeyId; }
    public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }

    public String getAccessKeySecret() { return accessKeySecret; }
    public void setAccessKeySecret(String accessKeySecret) { this.accessKeySecret = accessKeySecret; }

    public String getSecurityToken() { return securityToken; }
    public void setSecurityToken(String securityToken) { this.securityToken = securityToken; }

    public String getExpiration() { return expiration; }
    public void setExpiration(String expiration) { this.expiration = expiration; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
}

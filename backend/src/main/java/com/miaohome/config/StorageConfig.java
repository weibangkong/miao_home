package com.miaohome.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 * 读取 application.yml 中的 storage 配置段，按条件创建对应 Bean。
 */
@Configuration
public class StorageConfig {

    /**
     * 阿里云 OSS 配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "storage.aliyun")
    public AliyunOssProperties aliyunOssProperties() {
        return new AliyunOssProperties();
    }

    /**
     * 阿里云 OSS 客户端（仅当 storage.type=aliyun 时创建）
     * 使用长期访问密钥，用于服务端对象管理（上传 / 删除 / 读取）。
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "aliyun")
    public OSS ossClient(AliyunOssProperties props) {
        return new OSSClientBuilder().build(
                props.getEndpoint(), props.getAccessKeyId(), props.getAccessKeySecret());
    }

    /**
     * 阿里云 OSS 配置项
     */
    public static class AliyunOssProperties {
        /** OSS 服务端点，如 https://oss-cn-hangzhou.aliyuncs.com */
        private String endpoint;

        /** 地域，如 oss-cn-hangzhou */
        private String region;

        /** 存储桶名称 */
        private String bucket;

        /** 访问密钥 ID（RAM 子账号，需 OSS 读写 + sts:AssumeRole 权限） */
        private String accessKeyId;

        /** 访问密钥 Secret */
        private String accessKeySecret;

        /** RAM 角色 ARN（授予指定 bucket/prefix 的 PutObject 权限） */
        private String roleArn;

        /** STS 角色会话名称 */
        private String roleSessionName;

        /** 签名访问 URL 有效期（秒），默认 3600 */
        private long urlExpirationSeconds = 3600;

        /** 公共读对象访问前缀（如 https://bucket.oss-cn-hangzhou.aliyuncs.com），为空时由 endpoint + bucket 推导 */
        private String publicBaseUrl;

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }

        public String getAccessKeyId() { return accessKeyId; }
        public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }

        public String getAccessKeySecret() { return accessKeySecret; }
        public void setAccessKeySecret(String accessKeySecret) { this.accessKeySecret = accessKeySecret; }

        public String getRoleArn() { return roleArn; }
        public void setRoleArn(String roleArn) { this.roleArn = roleArn; }

        public String getRoleSessionName() { return roleSessionName; }
        public void setRoleSessionName(String roleSessionName) { this.roleSessionName = roleSessionName; }

        public long getUrlExpirationSeconds() { return urlExpirationSeconds; }
        public void setUrlExpirationSeconds(long urlExpirationSeconds) { this.urlExpirationSeconds = urlExpirationSeconds; }

        public String getPublicBaseUrl() { return publicBaseUrl; }
        public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    }
}

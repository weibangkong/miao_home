package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户多渠道认证实体
 * 一个用户可绑定多种认证方式（手机号、微信小程序等）。
 *
 * @author weibang kong
 */
@Entity
@Table(name = "mh_user_auth_provider", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_key"})
})
public class UserAuthProvider {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联用户 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 认证渠道：phone / wechat_miniapp */
    @Column(nullable = false, length = 30)
    private String provider;

    /** 渠道唯一标识：手机号 / 微信 openid */
    @Column(name = "provider_key", nullable = false, length = 200)
    private String providerKey;

    /** 渠道凭据：BCrypt 密码哈希 / 微信 unionid */
    @Column(length = 500)
    private String credential;

    /** 绑定时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserAuthProvider() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderKey() { return providerKey; }
    public void setProviderKey(String providerKey) { this.providerKey = providerKey; }

    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 支持手机号 + 密码登录、微信小程序登录，多渠道认证。
 *
 * @author weibang kong
 */
@Entity
@Table(name = "mh_user")
public class User {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 手机号，唯一。微信用户可为空 */
    @Column(length = 20, unique = true)
    private String phone;

    /** BCrypt 加密后的密码哈希。微信用户可为空 */
    @Column(name = "password_hash", length = 200)
    private String passwordHash;

    /** 用户昵称，对外显示 */
    @Column(nullable = false, length = 100)
    private String nickname;

    /** 头像地址 */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /** 用户类型：00-超级管理员，01-管理员，02-普通用户 */
    @Column(name = "user_type", nullable = false, length = 2)
    private String userType = "02";

    /** 记录创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户登录记录
 * 记录每个用户的最近一次登录时间。
 */
@Entity
@Table(name = "mh_user_login_record")
public class UserLoginRecord {

    /** 用户 ID（主键，关联 mh_user(id)） */
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 最近一次登录时间 */
    @Column(name = "last_login_at", nullable = false)
    private LocalDateTime lastLoginAt = LocalDateTime.now();

    /** 记录最后更新时间 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();


    // ---- 默认构造 ----
    public UserLoginRecord() {}


    // ---- getters / setters ----

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

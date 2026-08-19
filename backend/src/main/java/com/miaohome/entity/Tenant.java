package com.miaohome.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 租户（小区）
 */
@Entity
@Table(name = "mh_tenant")
public class Tenant {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 小区名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 小区编码（唯一标识，用于多租户路由） */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** 楼栋列表（逗号分隔，如 "A1,A2,B1"） */
    @Column(length = 50)
    private String building;

    /** 记录创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ---- 默认构造 ----
    public Tenant() {}

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

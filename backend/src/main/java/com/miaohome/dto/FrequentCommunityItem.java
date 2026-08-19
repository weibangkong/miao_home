package com.miaohome.dto;

/**
 * 经常出现小区 JSON 数组项
 * 表示一条"小区 + 楼栋"的记录。
 */
public class FrequentCommunityItem {

    /** 小区（租户）ID */
    private Long tenantId;

    /** 楼栋名称 */
    private String building;

    /** 小区名称（仅响应时填充，请求时可为 null） */
    private String tenantName;


    // ---- getters / setters ----

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
}

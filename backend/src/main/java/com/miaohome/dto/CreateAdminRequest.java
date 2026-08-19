package com.miaohome.dto;

/**
 * 创建超级管理员的请求体
 *
 * @author weibang kong
 */
public class CreateAdminRequest {

    /** 手机号 */
    private String phone;

    /** 密码（明文，至少 6 位） */
    private String password;

    /** 昵称（可选，默认"超级管理员"） */
    private String nickname;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}

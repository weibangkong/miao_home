package com.miaohome.dto;

/**
 * 登录请求
 *
 * @author weibang kong
 */
public class LoginRequest {

    /** 手机号 */
    private String phone;

    /** 明文密码 */
    private String password;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

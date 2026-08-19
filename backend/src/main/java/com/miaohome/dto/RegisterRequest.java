package com.miaohome.dto;

/**
 * 注册请求
 *
 * @author weibang kong
 */
public class RegisterRequest {

    /** 手机号 */
    private String phone;

    /** 明文密码 */
    private String password;

    /** 用户昵称 */
    private String nickname;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}

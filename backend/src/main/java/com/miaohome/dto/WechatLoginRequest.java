package com.miaohome.dto;

/**
 * 微信小程序登录请求
 *
 * @author weibang kong
 */
public class WechatLoginRequest {

    /** 小程序端 wx.login() 返回的临时 code */
    private String code;

    /** 用户昵称（可选，首次注册时使用） */
    private String nickname;

    /** 用户头像 URL（可选，首次注册时使用） */
    private String avatarUrl;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

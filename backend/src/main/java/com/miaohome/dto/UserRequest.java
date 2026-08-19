package com.miaohome.dto;

public class UserRequest {

    /** 用户昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatarUrl;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

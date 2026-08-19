package com.miaohome.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置
 * 对应 application.yml 中 wechat.miniapp 下的配置项。
 *
 * @author weibang kong
 */
@Configuration
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatConfig {

    /** 微信小程序 AppID */
    private String appid;

    /** 微信小程序 AppSecret */
    private String secret;

    public String getAppid() { return appid; }
    public void setAppid(String appid) { this.appid = appid; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}

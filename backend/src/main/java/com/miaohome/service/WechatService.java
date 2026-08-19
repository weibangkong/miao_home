package com.miaohome.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaohome.config.WechatConfig;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 微信 API 服务
 * 封装微信小程序 code → openid 换取逻辑。
 *
 * @author weibang kong
 */
@Service
public class WechatService {

    private static final Logger log = LoggerFactory.getLogger(WechatService.class);
    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final WechatConfig wechatConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WechatService(WechatConfig wechatConfig, ObjectMapper objectMapper) {
        this.wechatConfig = wechatConfig;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 微信登录结果
     */
    public static class WechatSession {
        private final String openid;
        private final String unionid;

        public WechatSession(String openid, String unionid) {
            this.openid = openid;
            this.unionid = unionid;
        }

        public String getOpenid() { return openid; }
        public String getUnionid() { return unionid; }
    }

    /**
     * 用小程序 code 换取 openid 和 unionid
     *
     * @param code 小程序端 wx.login() 返回的临时 code
     * @return WechatSession（含 openid、unionid）
     */
    public WechatSession code2session(String code) {
        String appid = wechatConfig.getAppid();
        String secret = wechatConfig.getSecret();

        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "微信小程序未配置，请联系管理员");
        }

        String url = CODE2SESSION_URL
                + "?appid=" + appid
                + "&secret=" + secret
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());

            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                String errmsg = node.has("errmsg") ? node.get("errmsg").asText() : "未知错误";
                log.error("微信 code2session 失败: errcode={}, errmsg={}", node.get("errcode").asInt(), errmsg);
                throw new BusinessException(ErrorCode.WECHAT_AUTH_FAILED, "微信登录失败: " + errmsg);
            }

            String openid = node.get("openid").asText();
            String unionid = node.has("unionid") ? node.get("unionid").asText() : null;

            return new WechatSession(openid, unionid);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信 code2session 请求异常", e);
            throw new BusinessException(ErrorCode.WECHAT_AUTH_FAILED, "微信登录服务异常，请稍后重试");
        }
    }
}

package com.miaohome.controller;

import com.miaohome.config.SessionContext;
import com.miaohome.dto.ApiResult;
import com.miaohome.dto.UserAvatarRequest;
import com.miaohome.dto.UserResponse;
import com.miaohome.dto.LoginRequest;
import com.miaohome.dto.RegisterRequest;
import com.miaohome.dto.WechatLoginRequest;
import com.miaohome.entity.User;
import com.miaohome.service.UserLoginRecordService;
import com.miaohome.service.UserService;
import com.miaohome.service.WechatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户认证接口
 * 提供手机号注册/登录、微信小程序登录、账号绑定、登出等功能。
 *
 * @author weibang kong
 */
@Tag(name = "用户认证", description = "注册、登录、登出、微信绑定与用户信息查询")
@RestController
@RequestMapping("/users")
public class AuthController {

    private final UserService userService;
    private final WechatService wechatService;
    private final UserLoginRecordService userLoginRecordService;

    public AuthController(UserService userService,
                          WechatService wechatService,
                          UserLoginRecordService userLoginRecordService) {
        this.userService = userService;
        this.wechatService = wechatService;
        this.userLoginRecordService = userLoginRecordService;
    }

    /**
     * 注册
     */
    @Operation(summary = "手机号注册")
    @PostMapping(value = "/register", consumes = "application/json")
    public ApiResult<UserResponse> register(@Parameter(description = "注册信息") @RequestBody RegisterRequest request) {
        User user = userService.register(request.getPhone(), request.getPassword(), request.getNickname());
        return ApiResult.success(userService.toUserResponse(user));
    }

    /**
     * 登录
     */
    @Operation(summary = "手机号登录")
    @PostMapping(value = "/login", consumes = "application/json")
    public ApiResult<UserResponse> login(@Parameter(description = "登录信息") @RequestBody LoginRequest request, HttpSession session) {
        User user = userService.login(request.getPhone(), request.getPassword());
        SessionContext.setUserId(session, user.getId());
        userLoginRecordService.recordLogin(user.getId());
        return ApiResult.success(userService.toUserResponse(user));
    }

    /**
     * 登出
     */
    @Operation(summary = "登出")
    @PostMapping(value = "/logout", consumes = "application/json")
    public ApiResult<Void> logout(HttpSession session) {
        SessionContext.removeUserId(session);
        session.invalidate();
        return ApiResult.success(null);
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public ApiResult<UserResponse> getCurrentUser() {
        Long userId = SessionContext.getUserId();
        User user = userService.getUserById(userId);
        return ApiResult.success(userService.toUserResponse(user));
    }

    /**
     * 更新当前登录用户头像（回传 OSS 对象键）
     */
    @Operation(summary = "更新当前登录用户头像")
    @PutMapping(value = "/me/avatar", consumes = "application/json")
    public ApiResult<UserResponse> updateAvatar(
            @Parameter(description = "头像对象键") @RequestBody UserAvatarRequest request) {
        Long userId = SessionContext.getUserId();
        User user = userService.updateAvatar(userId, request.getObjectKey());
        return ApiResult.success(userService.toUserResponse(user));
    }

    /**
     * 根据 ID 获取用户公开信息
     */
    @Operation(summary = "根据 ID 获取用户公开信息")
    @GetMapping("/{userId}")
    public ApiResult<UserResponse> getUser(@Parameter(description = "用户 ID") @PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ApiResult.success(userService.toUserResponse(user));
    }


    /**
     * 微信小程序登录 / 注册
     * 用 wx.login() 返回的 code 换取 openid，自动登录或创建新用户。
     */
    @Operation(summary = "微信小程序登录/注册")
    @PostMapping(value = "/wechat/login", consumes = "application/json")
    public ApiResult<UserResponse> wechatLogin(@Parameter(description = "微信登录请求") @RequestBody WechatLoginRequest request, HttpSession session) {
        WechatService.WechatSession wxSession = wechatService.code2session(request.getCode());
        User user = userService.registerByWechat(
                wxSession.getOpenid(),
                wxSession.getUnionid(),
                request.getNickname(),
                request.getAvatarUrl());
        SessionContext.setUserId(session, user.getId());
        userLoginRecordService.recordLogin(user.getId());
        return ApiResult.success(userService.toUserResponse(user));
    }


    /**
     * 给当前登录用户绑定微信
     */
    @Operation(summary = "给当前登录用户绑定微信")
    @PostMapping(value = "/wechat/bind", consumes = "application/json")
    public ApiResult<Void> bindWechat(@Parameter(description = "微信绑定请求") @RequestBody WechatLoginRequest request) {
        Long userId = SessionContext.getUserId();
        WechatService.WechatSession wxSession = wechatService.code2session(request.getCode());
        userService.bindWechat(userId, wxSession.getOpenid(), wxSession.getUnionid());
        return ApiResult.success(null);
    }


    /**
     * 获取当前用户已绑定的认证渠道列表
     */
    @Operation(summary = "获取当前用户已绑定的认证渠道列表")
    @GetMapping("/bindings/list")
    public ApiResult<List<String>> getBindings() {
        Long userId = SessionContext.getUserId();
        List<String> bindings = userService.getUserBindings(userId);
        return ApiResult.success(bindings);
    }
}

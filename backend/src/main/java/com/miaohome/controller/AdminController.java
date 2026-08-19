package com.miaohome.controller;

import com.miaohome.dto.ApiResult;
import com.miaohome.dto.CreateAdminRequest;
import com.miaohome.dto.UserResponse;
import com.miaohome.entity.User;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 管理后台接口
 * 仅允许本机 localhost 调用，用于运维操作。
 *
 * @author weibang kong
 */
@Tag(name = "管理后台", description = "超级管理员运维接口，仅限本机 localhost 调用")
@RestController
@RequestMapping("/admin")
public class AdminController {

    /** 允许的本地回环地址 */
    private static final Set<String> LOCALHOST_ADDRESSES = Set.of(
            "127.0.0.1",
            "0:0:0:0:0:0:0:1"
    );

    /** IPv6 本地地址前缀 */
    private static final String IPV6_LOCALHOST_PREFIX = "0:0:0:0:0:0:0:1";

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }


    /**
     * 创建或升级超级管理员账号（仅限 localhost 访问）
     */
    @Operation(summary = "创建或升级超级管理员账号")
    @PostMapping(value = "/super/admins", consumes = "application/json")
    public ApiResult<UserResponse> createSuperAdmin(
            @Parameter(description = "超级管理员账号信息") @RequestBody CreateAdminRequest request,
            HttpServletRequest httpRequest) {
        assertLocalhost(httpRequest);

        User user = userService.createSuperAdmin(
                request.getPhone(),
                request.getPassword(),
                request.getNickname());

        return ApiResult.success(userService.toUserResponse(user));
    }


    /**
     * 校验请求来源为 localhost，否则抛出 403
     */
    private void assertLocalhost(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null || !LOCALHOST_ADDRESSES.contains(remoteAddr)) {
            if (remoteAddr != null && remoteAddr.startsWith(IPV6_LOCALHOST_PREFIX)) {
                return;
            }
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅允许本机 localhost 访问");
        }
    }
}

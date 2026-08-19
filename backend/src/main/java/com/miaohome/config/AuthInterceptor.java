package com.miaohome.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * 对需要登录的写操作接口校验会话中的用户 ID，未登录则返回 401。
 *
 * @author weibang kong
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // GET 请求公开访问
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 登录、注册、微信登录和管理后台接口不拦截（管理后台有 localhost 限制）
        String path = request.getRequestURI();
        if (path.endsWith("/users/login") || path.endsWith("/users/register")
                || path.endsWith("/users/wechat/login")
                || path.startsWith("/miaohome/api/admin/")) {
            return true;
        }

        // 其他 POST/PUT/DELETE 请求需要登录
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":1009,\"message\":\"请先登录\",\"data\":null}");
            return false;
        }

        return true;
    }
}

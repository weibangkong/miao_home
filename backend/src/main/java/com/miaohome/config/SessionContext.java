package com.miaohome.config;

import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

/**
 * 会话上下文工具类
 * 从当前请求的 HttpSession 中获取登录用户 ID。
 *
 * @author weibang kong
 */
public class SessionContext {

    private static final String USER_ID_KEY = "userId";

    private SessionContext() {}

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户 ID
     * @throws BusinessException 如果未登录
     */
    public static Long getUserId() {
        HttpSession session = getCurrentSession();
        Long userId = (Long) session.getAttribute(USER_ID_KEY);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return userId;
    }

    /**
     * 检查当前请求是否已登录
     *
     * @return true 如果已登录
     */
    public static boolean isLoggedIn() {
        try {
            HttpSession session = getCurrentSession();
            return session.getAttribute(USER_ID_KEY) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置登录用户 ID
     */
    public static void setUserId(HttpSession session, Long userId) {
        session.setAttribute(USER_ID_KEY, userId);
    }

    /**
     * 清除登录状态
     */
    public static void removeUserId(HttpSession session) {
        session.removeAttribute(USER_ID_KEY);
    }

    private static HttpSession getCurrentSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "无法获取当前请求上下文");
        }
        return attrs.getRequest().getSession();
    }
}

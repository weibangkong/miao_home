package com.miaohome.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantHeader = request.getHeader("X-Tenant-Id");
            if (tenantHeader != null && !tenantHeader.isEmpty()) {
                try {
                    TenantContext.setTenantId(Long.parseLong(tenantHeader));
                } catch (NumberFormatException ignored) {}
            }
            // 不设置 → getTenantId() 返回 null → 表示全部小区
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

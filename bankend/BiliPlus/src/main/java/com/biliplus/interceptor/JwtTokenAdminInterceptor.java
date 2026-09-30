package com.biliplus.interceptor;

import com.biliplus.properties.JwtProperties;
import com.biliplus.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截的是controller方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            return true; // 静态资源放行
        }

        // 获取请求的令牌
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            log.warn("管理员请求缺少 Authorization 头");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long adminId = JwtUtil.extractAdminId(claims);
            if (adminId == null) {
                log.warn("管理员 JWT 中未包含有效的 adminId");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            log.info("当前登录管理员 ID: {}", adminId);
            request.setAttribute("currentAdminId", adminId);
            // 操作日志埋点直接读 ThreadLocal，避免把 adminId/IP 透传到 Service 签名
            com.biliplus.utils.AdminContext.setAdminId(adminId);
            com.biliplus.utils.AdminContext.setClientIp(resolveClientIp(request));
            return true;
        } catch (Exception e) {
            log.warn("管理员 JWT 验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    /** 取真实来源 IP，兼容反向代理后的 X-Forwarded-For */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // 取第一个，即最靠近客户端的一跳
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

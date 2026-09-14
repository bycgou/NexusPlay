package com.biliplus.interceptor;

import com.biliplus.properties.JwtPeopleProperties;
import com.biliplus.utils.JwtUtil;
import com.biliplus.utils.UserContext;
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
public class JwtTokenPeopleInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtPeopleProperties jwtPeopleProperties;

    /** 允许匿名 GET 浏览的路径前缀（写操作仍需登录） */
    private static final String[] PUBLIC_GET_PREFIXES = {
            "/pp/videos/",
            "/pp/comments",
            "/pp/user/danmakuv3",
            "/pp/categories",
            "/pp/banners",
            "/pp/people/user/",
            "/pp/people/search",
            "/pp/interaction/video/",
            "/pp/interaction/user/"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            for (String prefix : PUBLIC_GET_PREFIXES) {
                if (path.endsWith(prefix) || path.contains(prefix)) {
                    return true;
                }
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            log.warn("请求缺少 Authorization 头: {} {}", method, path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtPeopleProperties.getPeopleSecretKey(), token);
            Long userId = JwtUtil.extractUserId(claims);
            if (userId == null) {
                log.warn("JWT 中未包含有效的 userId");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            UserContext.setCurrentUserId(userId);
            request.setAttribute("currentUserId", userId);
            return true;
        } catch (Exception e) {
            log.warn("JWT 验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}

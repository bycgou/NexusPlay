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
            "/pp/interaction/user/",
            "/pp/live/rooms",
            "/pp/live/gifts",
            "/pp/live/pk/active",
            "/pp/live/rooms/", // 含详情/stream-status/mic history 等 GET
            "/pp/anime" // 番剧列表/详情
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            boolean isPublicGet = false;
            for (String prefix : PUBLIC_GET_PREFIXES) {
                if (path.endsWith(prefix) || path.contains(prefix)) {
                    isPublicGet = true;
                    break;
                }
            }
            if (isPublicGet) {
                // 公开 GET 也要尽量解析 token，否则「是否已关注/已点赞」永远拿不到当前用户
                trySetCurrentUserFromHeader(request);
                return true;
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

    /** 公开接口：有 token 则注入当前用户，无 token / 失败也放行 */
    private void trySetCurrentUserFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return;
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            Claims claims = JwtUtil.parseJWT(jwtPeopleProperties.getPeopleSecretKey(), token);
            Long userId = JwtUtil.extractUserId(claims);
            if (userId != null) {
                UserContext.setCurrentUserId(userId);
                request.setAttribute("currentUserId", userId);
            }
        } catch (Exception e) {
            // 匿名浏览场景忽略无效 token
        }
    }
}

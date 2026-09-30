package com.biliplus.interceptor;

import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.pojo.entity.User;
import com.biliplus.properties.JwtPeopleProperties;
import com.biliplus.service.UserPenaltyService;
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

    @Autowired
    private UserPenaltyService userPenaltyService;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    /** 需要登录才能访问的 GET 路径前缀（其余 GET 默认公开，支持免登录浏览） */
    private static final String[] PRIVATE_GET_PREFIXES = {
            "/pp/chat",
            "/pp/notifications", // 未读数单独放行
            "/pp/play-history",
            "/pp/favorite-folders",
            "/pp/live/wallet",
            "/pp/live/my-room",
            "/pp/people/my",
            "/pp/people/me",
            "/pp/people/settings",
            "/pp/interaction/my",
            "/pp/dynamics/feed", // 关注流
            "/pp/reports"
    };

    /** 允许匿名 POST 的路径（分享计数等只读语义的埋点） */
    private static final java.util.regex.Pattern PUBLIC_POST = java.util.regex.Pattern.compile(
            "^/pp/videos/\\d+/share$"
    );

    /** 未读红点等个别 GET 需放行，避免顶栏未登录报错 */
    private static final java.util.regex.Pattern PUBLIC_GET_EXACT = java.util.regex.Pattern.compile(
            "^/pp/notifications/unread-count$"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            boolean isPrivateGet = false;
            for (String prefix : PRIVATE_GET_PREFIXES) {
                // 仅前缀匹配，避免 path.contains 误伤
                if (path.startsWith(prefix)) {
                    isPrivateGet = true;
                    break;
                }
            }
            if (PUBLIC_GET_EXACT.matcher(path).matches()) {
                isPrivateGet = false;
            }
            // 公开 GET 也要尽量解析 token，否则「是否已关注/已点赞」永远拿不到当前用户
            if (!isPrivateGet) {
                trySetCurrentUserFromHeader(request);
                return true;
            }
        }

        if ("POST".equalsIgnoreCase(method) && PUBLIC_POST.matcher(path).matches()) {
            trySetCurrentUserFromHeader(request);
            return true;
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
            // 封禁用户立即拒绝，避免旧 token 在封禁后继续调用写接口
            if (isBlocked(userId)) {
                log.warn("封禁用户尝试访问: userId={}, {} {}", userId, method, path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":0,\"msg\":\"账号已被封禁，如有疑问请联系管理员\",\"data\":null}");
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

    private boolean isBlocked(Long userId) {
        if (userPenaltyService.isBanned(userId)) {
            return true;
        }
        User user = peopleUserMapper.getUserById(userId);
        return user != null && user.getStatus() != null && user.getStatus() == 0;
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

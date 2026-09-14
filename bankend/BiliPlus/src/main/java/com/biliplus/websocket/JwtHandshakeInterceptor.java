package com.biliplus.websocket;

import com.biliplus.properties.JwtPeopleProperties;
import com.biliplus.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtPeopleProperties jwtPeopleProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        String token = null;
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            token = query.split("token=")[1].split("&")[0];
        }

        if (token == null || token.isEmpty()) {
            log.warn("WebSocket 握手失败：缺少 token");
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtPeopleProperties.getPeopleSecretKey(), token);
            Long userId = JwtUtil.extractUserId(claims);
            if (userId == null) {
                log.warn("JWT 中未包含 userId");
                return false;
            }
            attributes.put("userId", userId);
            log.info("WebSocket 握手成功，用户 ID: {}", userId);
            return true;
        } catch (Exception e) {
            log.warn("JWT 验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 可选
        log.info("WebSocket 握手结束");
    }
}
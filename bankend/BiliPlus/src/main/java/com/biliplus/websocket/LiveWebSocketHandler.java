package com.biliplus.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 直播房间 WebSocket：/ws/live?token=
 * 协议见 LIVE_FULL_PLAN.md §3.3
 */
@Slf4j
@Component
public class LiveWebSocketHandler extends TextWebSocketHandler {

    /** roomId -> sessions */
    private static final Map<Long, Set<WebSocketSession>> ROOM_SESSIONS = new ConcurrentHashMap<>();
    /** sessionId -> roomId */
    private static final Map<String, Long> SESSION_ROOM = new ConcurrentHashMap<>();
    /** sessionId -> userId */
    private static final Map<String, Long> SESSION_USER = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private LiveMessageDispatcher dispatcher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_USER.put(session.getId(), userId);
            log.info("直播 WS 上线 user={}, session={}", userId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = SESSION_USER.get(session.getId());
        if (userId == null) {
            userId = (Long) session.getAttributes().get("userId");
        }
        if (userId == null) {
            sendText(session, errorJson("未认证用户"));
            return;
        }
        try {
            dispatcher.dispatch(session, userId, message.getPayload());
        } catch (Exception e) {
            log.error("直播消息处理失败 userId={}", userId, e);
            sendText(session, errorJson("消息处理失败"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sid = session.getId();
        Long roomId = SESSION_ROOM.remove(sid);
        SESSION_USER.remove(sid);
        if (roomId != null) {
            Set<WebSocketSession> set = ROOM_SESSIONS.get(roomId);
            if (set != null) {
                set.remove(session);
                broadcastOnline(roomId);
            }
            log.info("直播 WS 下线 session={}, roomId={}", sid, roomId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("直播 WS 传输错误", exception);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException ignored) {
        }
    }

    // ===== 公开工具 =====

    public void joinRoom(WebSocketSession session, Long roomId) {
        leaveRoom(session);
        ROOM_SESSIONS.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        SESSION_ROOM.put(session.getId(), roomId);
        broadcastOnline(roomId);
    }

    public void leaveRoom(WebSocketSession session) {
        Long roomId = SESSION_ROOM.remove(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> set = ROOM_SESSIONS.get(roomId);
            if (set != null) {
                set.remove(session);
                broadcastOnline(roomId);
            }
        }
    }

    public void broadcast(Long roomId, String json) {
        Set<WebSocketSession> set = ROOM_SESSIONS.get(roomId);
        if (set == null || set.isEmpty()) {
            return;
        }
        for (WebSocketSession s : set) {
            sendText(s, json);
        }
    }

    public void broadcastToRooms(Long roomId1, Long roomId2, String json) {
        if (roomId1 != null) {
            broadcast(roomId1, json);
        }
        if (roomId2 != null && !roomId2.equals(roomId1)) {
            broadcast(roomId2, json);
        }
    }

    public int onlineCount(Long roomId) {
        Set<WebSocketSession> set = ROOM_SESSIONS.get(roomId);
        return set == null ? 0 : set.size();
    }

    public void clearRoom(Long roomId) {
        Set<WebSocketSession> set = ROOM_SESSIONS.remove(roomId);
        if (set != null) {
            for (WebSocketSession s : set) {
                SESSION_ROOM.remove(s.getId());
            }
        }
    }

    public Long getUserId(WebSocketSession session) {
        Long uid = SESSION_USER.get(session.getId());
        if (uid != null) {
            return uid;
        }
        return (Long) session.getAttributes().get("userId");
    }

    public Long getRoomId(WebSocketSession session) {
        return SESSION_ROOM.get(session.getId());
    }

    private void broadcastOnline(Long roomId) {
        int count = onlineCount(roomId);
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "online");
            node.put("roomId", roomId);
            node.put("count", count);
            broadcast(roomId, node.toString());
        } catch (Exception e) {
            log.warn("广播 online 失败 roomId={}", roomId, e);
        }
    }

    private void sendText(WebSocketSession session, String json) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("直播 WS 发送失败 session={}", session.getId(), e);
            }
        }
    }

    private String errorJson(String msg) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("error", msg);
            return node.toString();
        } catch (Exception e) {
            return "{\"error\":\"" + msg + "\"}";
        }
    }
}

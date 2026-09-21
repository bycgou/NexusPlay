package com.biliplus.websocket;

import com.biliplus.exception.BusinessException;
import com.biliplus.pojo.entity.LiveMicSession;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.pojo.entity.User;
import com.biliplus.mapper.LiveRoomMapper;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.service.LiveMicService;
import com.biliplus.properties.LiveProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/** 分发 /ws/live 客户端消息 */
@Slf4j
@Component
public class LiveMessageDispatcher {

    @Autowired
    @Lazy
    private LiveWebSocketHandler liveWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LiveRoomMapper liveRoomMapper;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    @Autowired
    private LiveMicService liveMicService;

    @Autowired
    private LiveProperties liveProperties;

    public void dispatch(WebSocketSession session, Long userId, String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        String type = root.path("type").asText("");
        Long roomId = root.path("roomId").isNull() || root.path("roomId").isMissingNode()
                ? null : root.path("roomId").asLong();

        switch (type) {
            case "join" -> {
                if (roomId == null) {
                    throw new BusinessException("缺少 roomId");
                }
                liveWebSocketHandler.joinRoom(session, roomId);
                ObjectNode ack = objectMapper.createObjectNode();
                ack.put("type", "join_ok");
                ack.put("roomId", roomId);
                session.sendMessage(new TextMessage(ack.toString()));
            }
            case "leave" -> liveWebSocketHandler.leaveRoom(session);
            case "chat" -> handleChat(session, userId, roomId, root.path("content").asText(""));
            case "mic_apply" -> handleMicApply(session, userId, roomId);
            case "mic_accept" -> handleMicAccept(session, userId, root.path("applyUserId").asLong(), root.path("agree").asBoolean(true));
            case "mic_leave" -> handleMicLeave(session, userId, roomId);
            default -> session.sendMessage(new TextMessage("{\"error\":\"未知消息类型\"}"));
        }
    }

    private void handleChat(WebSocketSession session, Long userId, Long roomId, String content) throws Exception {
        if (roomId == null || !StringUtils.hasText(content) || content.length() > 200) {
            session.sendMessage(new TextMessage("{\"error\":\"聊天参数非法\"}"));
            return;
        }
        User user = peopleUserMapper.getUserById(userId);
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "chat");
        node.put("roomId", roomId);
        node.put("userId", userId);
        node.put("nickname", resolveNickname(user));
        node.put("content", content);
        node.put("ts", System.currentTimeMillis());
        liveWebSocketHandler.broadcast(roomId, node.toString());
    }

    private void handleMicApply(WebSocketSession session, Long userId, Long roomId) throws Exception {
        if (roomId == null) {
            session.sendMessage(new TextMessage("{\"error\":\"缺少 roomId\"}"));
            return;
        }
        LiveMicSession micSession = liveMicService.apply(userId, roomId);
        User user = peopleUserMapper.getUserById(userId);
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "mic_apply");
        node.put("roomId", roomId);
        node.put("userId", userId);
        node.put("nickname", resolveNickname(user));
        node.put("sessionId", micSession.getId());
        liveWebSocketHandler.broadcast(roomId, node.toString());
    }

    private void handleMicAccept(WebSocketSession session, Long hostId, Long applyUserId, boolean agree) throws Exception {
        // 由 HTTP 风格参数补全：applyUserId 即 guest，sessionId 在 service 内查 pending
        // 这里通过 roomId 从 session 取
        Long roomId = liveWebSocketHandler.getRoomId(session);
        if (roomId == null) {
            session.sendMessage(new TextMessage("{\"error\":\"请先 join 房间\"}"));
            return;
        }
        var pending = liveMicService.listPending(roomId);
        LiveMicSession target = pending.stream()
                .filter(s -> s.getGuestUserId() != null && s.getGuestUserId().equals(applyUserId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            session.sendMessage(new TextMessage("{\"error\":\"未找到连麦申请\"}"));
            return;
        }
        Map<String, Object> result = liveMicService.accept(hostId, target.getId(), agree);
        // Service 内会广播 mic_ready / 结果
    }

    private void handleMicLeave(WebSocketSession session, Long userId, Long roomId) {
        if (roomId == null) {
            roomId = liveWebSocketHandler.getRoomId(session);
        }
        if (roomId != null) {
            liveMicService.leave(userId, roomId);
        }
    }

    private static String resolveNickname(User user) {
        if (user == null) {
            return "用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        return StringUtils.hasText(user.getUsername()) ? user.getUsername() : ("用户" + user.getId());
    }
}

// src/main/java/com/biliplus/websocket/ChatWebSocketHandler.java
package com.biliplus.websocket;

import com.biliplus.pojo.dto.ChatMessageDTO;
import com.biliplus.pojo.entity.ChatMessage;
import com.biliplus.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 存储用户ID与WebSocket会话的映射（ConcurrentHashMap保证线程安全）
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    // 使用 Spring 注入的 ObjectMapper（支持更多配置）
    @Autowired
    private ObjectMapper objectMapper;

    // 注入聊天业务服务（处理消息存储、会话查询等）
    @Autowired
    private ChatService chatService;


    // 连接建立时的逻辑
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从WebSocket会话属性中获取已认证的用户ID（通常由拦截器提前设置，如Token解析后存入）
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            // 将用户ID与会话绑定，存入线程安全的Map
            USER_SESSIONS.put(userId, session);
            log.info("用户 {} 上线 WebSocket，当前在线人数: {}", userId, USER_SESSIONS.size());
        }
    }

    // 处理客户端发送的文本消息
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 获取发送者ID
        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) {
            session.sendMessage(new TextMessage("{\"error\":\"未认证用户\"}"));
            return;
        }

        try {
            // ✅ 使用 DTO 反序列化（自动处理缺失字段为 null）
            // 将客户端发送的JSON消息反序列化为ChatMessageDTO（数据传输对象）
            ChatMessageDTO dto = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);

            // ===============================webRTC信令处理(msgType=0)==================
            if(dto.getMsgType()!=null && dto.getMsgType()==0){
                log.info("收到用户 {} 的webRTC信令: content={}, conversationId={}", senderId, dto.getContent(), dto.getConversationId());
                Long receiverId = chatService.selectOtherUserIdInPrivate(dto.getConversationId(), senderId);
                if (receiverId != null && dto.getExtraData() != null) {
                    // 直接透传 extraData 给对方
                    ObjectNode forwardMsg = objectMapper.createObjectNode();
                    forwardMsg.put("conversationId", dto.getConversationId());
                    forwardMsg.put("msgType", (byte) 0);
                    forwardMsg.put("extraData", dto.getExtraData()); // 原样转发
                    forwardMsg.put("senderId", senderId); // 可选：用于前端识别来源

                    sendMessageToUser(receiverId, forwardMsg.toString());
                }

                return;
            }
            

            // 字段校验
            if (dto.getConversationId() == null || dto.getContent() == null || dto.getMsgType() == null) {
                session.sendMessage(new TextMessage("{\"error\":\"缺少必要字段: conversationId, content, msgType\"}"));
                return;
            }

            log.info("收到用户 {} 的消息: content={}, conversationId={}", senderId, dto.getContent(), dto.getConversationId());

            // TODO: 调用 Service 保存消息
            ChatMessage  msg = chatService.saveMessage(senderId, dto.getConversationId(), dto.getContent(), dto.getMsgType());

            // 构造响应（使用真实消息ID）
            ObjectNode response = objectMapper.createObjectNode();
            response.put("id", msg.getId());
            response.put("senderId", senderId);
            response.put("conversationId", dto.getConversationId());
            response.put("content", dto.getContent());
            response.put("msgType", dto.getMsgType());
            response.put("createdAt", msg.getCreateTime() != null
                    ? msg.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis());

            // 获取接收者ID并推送
            Long receiverId = chatService.selectOtherUserIdInPrivate(dto.getConversationId(), senderId);
            log.info("接受者ID:{}", receiverId);
            if (receiverId != null) {
                sendMessageToUser(receiverId, response.toString());
            } else {
                log.warn("未找到 conversationId={} 中除 senderId={} 外的其他成员", dto.getConversationId(), senderId);
            }
            // 回传发送方，便于多端同步
            sendMessageToUser(senderId, response.toString());

        } catch (Exception e) {
            log.error("处理消息异常", e);
            session.sendMessage(new TextMessage("{\"error\":\"消息格式错误或处理失败\"}"));
        }
    }

    // 连接关闭时的逻辑
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("用户 {} 下线 WebSocket", userId);
        }
    }

    // 处理传输错误
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误", exception);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException ignored) {}
    }

    // ===== 工具方法 =====

    public void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败 to user: {}", userId, e);
                USER_SESSIONS.remove(userId); // 移除失效连接
            }
        }
    }

    public static void broadcastMessage(Long userId, String message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("广播消息失败 to user: {}", userId, e);
            }
        }
    }
}
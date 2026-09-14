package com.biliplus.controller.user;

import com.biliplus.pojo.dto.userdto.ChatConversationDTO;
import com.biliplus.pojo.dto.userdto.CreateConversationDTO;
import com.biliplus.pojo.entity.ChatConversation;
import com.biliplus.pojo.entity.ChatMessage;
import com.biliplus.pojo.vo.ChatVO;
import com.biliplus.result.Result;
import com.biliplus.service.ChatService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController()
@RequestMapping("/pp/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 获取会话列表
    // 返回用户参与的所有会话 + otherUser 信息
    @GetMapping("/conversations")
    public Result<List<ChatConversationDTO>> getConversationList() {
        log.info("--获取会话列表--");
        Long userid = UserContext.getCurrentUserId();
        List<ChatConversationDTO> conversations = chatService.getConversationList(userid);
        return Result.success(conversations);
    }

    /** 顶栏红点：当前用户未读私信总数 */
    @GetMapping("/unread-total")
    public Result<Integer> unreadTotal() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.success(0);
        }
        return Result.success(chatService.countUnread(userId));
    }

    // 创建私聊会话
    // 输入 targetUserId，返回会话（自动创建成员）
    @PostMapping("/conversations/private")
    public Result<ChatConversationDTO> createPrivateConversation(@RequestBody CreateConversationDTO createConversationDTO) {
        Long targetUserId = createConversationDTO.getTargetUserId();
        log.info("创建私聊会话,对象id:{}", targetUserId);
        Long userid = UserContext.getCurrentUserId();
        log.info("通过ThreadLocal获取到的当前登录用户Id{}", userid);

        if (targetUserId == null) {
            return Result.error("请指定会话对象");
        }
        ChatConversation conversation = chatService.createConversations(userid, targetUserId);
        ChatConversationDTO.UserVO otherUser = chatService.getOtherUser(targetUserId);
        ChatConversationDTO dto = ChatConversationDTO.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .otherUser(otherUser)
                .lastMessage(null)
                .lastMessageAt(null)
                .unreadCount(0)
                .build();
        log.info("返回的dto:{}", dto);
        return Result.success(dto);
    }

    // 获取会话消息（分页）
    @GetMapping("/conversations/{convId}/messages")
    public Result getConversationMessage(
            @PathVariable Long convId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        log.info("获取会话消息: convId={}, page={}, pageSize={}", convId, page, pageSize);
        if (convId == null) {
            return Result.error("请指定会话");
        }
        Long userId = UserContext.getCurrentUserId();
        try {
            List<ChatMessage> messages = chatService.getMessage(convId, page, pageSize, userId);
            return Result.success(messages);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    // 发送消息（HTTP 接口，通常通过 WebSocket 发送，此接口作为备用）
    @PostMapping("/messages")
    public Result<ChatMessage> sendMessage(@RequestBody Map<String, Object> body) {
        Long conversationId = Long.valueOf(body.get("conversationId").toString());
        String content = body.get("content").toString();
        Byte msgType = Byte.valueOf(body.getOrDefault("msgType", "1").toString());

        Long senderId = UserContext.getCurrentUserId();
        log.info("HTTP发送消息: senderId={}, conversationId={}", senderId, conversationId);

        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }

        ChatMessage message;
        try {
            message = chatService.saveMessage(senderId, conversationId, content, msgType);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
        return Result.success(message);
    }

    // 标记已读
    // 更新 lastReadMsgId 和 unreadCount=0
    @PostMapping("/conversations/{id}/read")
    public Result markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        log.info("标记会话已读: conversationId={}, userId={}", id, userId);
        chatService.markAsRead(id, userId);
        return Result.success("标记成功");
    }
}

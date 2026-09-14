package com.biliplus.service;

import com.biliplus.pojo.dto.ChatMessageDTO;
import com.biliplus.pojo.dto.userdto.ChatConversationDTO;
import com.biliplus.pojo.entity.ChatConversation;
import com.biliplus.pojo.entity.ChatMessage;
import com.biliplus.pojo.entity.User;

import java.util.List;

public interface ChatService {

    boolean isUserInConversation(Long userId, Long conversationId);
    Iterable<Long> getReceiverUserIds(Long conversationId, Long senderId);

    ChatConversation createConversations(Long userid, Long targetUserId);

    ChatConversationDTO.UserVO getOtherUser(Long targetUserId);

    List<ChatConversationDTO> getConversationList(Long userid);

    /**
     * 获取会话消息（分页）
     * @param convId 会话ID
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @param userId 当前登录用户（校验成员身份）
     */
    List<ChatMessage> getMessage(Long convId, int page, int pageSize, Long userId);


    ChatMessage saveMessage(Long senderId, Long conversationId, String content, Byte msgType);

    Long selectOtherUserIdInPrivate(Long conversationId, Long senderId);

    /**
     * 标记会话已读：更新 lastReadMsgId 并将 unreadCount 置 0
     */
    void markAsRead(Long conversationId, Long userId);

    /** 当前用户未读私信总数 */
    int countUnread(Long userId);
}

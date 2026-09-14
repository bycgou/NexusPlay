package com.biliplus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.biliplus.pojo.entity.ChatConversationMember;

import java.util.List;

@Mapper
public interface ChatConversationMemberMapper {
    // 查询某会话中除 sender 外的所有在线成员（用于推送）
    List<Long> selectOtherMemberUserIds(
            @Param("conversationId") Long conversationId,
            @Param("excludeUserId") Long excludeUserId
    );

    // 检查用户是否在会话中（且未退出）
    int countActiveMember(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

    // 更新未读数（+1）
    void incrementUnreadCount(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

    // 重置未读数为0
    void resetUnreadCount(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

    // 更新最后已读消息ID
    void updateLastReadMsgId(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            @Param("msgId") Long msgId
    );

    // 插入回话成员
    void insert(ChatConversationMember build);

    // 查找私聊会话成员
    Long selectOtherMemberUserId(Long conversationId, Long senderId);

    /** 当前用户全部会话未读总数 */
    @org.apache.ibatis.annotations.Select(
            "SELECT COALESCE(SUM(unread_count), 0) FROM chat_conversation_member " +
            "WHERE user_id = #{userId} AND leave_time IS NULL")
    int sumUnreadCount(@Param("userId") Long userId);
}

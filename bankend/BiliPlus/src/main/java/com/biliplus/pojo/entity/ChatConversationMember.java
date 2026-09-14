package com.biliplus.pojo.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class ChatConversationMember {
    private Long id;
    private Long conversationId;
    private Long userId;
    private Integer unreadCount;        // 未读数
    private Long lastReadMsgId;         // 最后已读消息ID
    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;    // 退出时间，null 表示未退出
}

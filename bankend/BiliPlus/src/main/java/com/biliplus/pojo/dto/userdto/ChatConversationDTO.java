package com.biliplus.pojo.dto.userdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChatConversationDTO {
    private Long id;
    private Byte type;
    private UserVO otherUser; // 私聊时才有
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class UserVO {
        private Long id;
        private String name;
        private String avatar;
    }
}

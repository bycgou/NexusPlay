package com.biliplus.pojo.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ChatConversation {
    private Long id;
    private Byte type;           // 1-私聊, 2-群聊
    private String name;         // 群聊名称（私聊为 null）
    private String avatar;       // 群头像（私聊为 null）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

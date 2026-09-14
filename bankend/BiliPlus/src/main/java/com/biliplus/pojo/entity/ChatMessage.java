package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;  // 自增
    private Long conversationId;  //
    private Long senderId;  // 发送者
    private String content;        // JSON 格式富文本
    private Byte msgType;          // 1-文本, 2-图片, 3-链接, 4-表情...
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime;  // 更新时间
}

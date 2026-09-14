package com.biliplus.pojo.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long conversationId;
    private String content;
    private Byte msgType;      //0: WebRTC 信令（新增约定）,1: 文本, 2: 图片, etc.
    // 新增：用于传输 WebRTC 信令的 JSON 字符串（如 SDP、ICE candidate）
    private String extraData;        // 可为 null
}

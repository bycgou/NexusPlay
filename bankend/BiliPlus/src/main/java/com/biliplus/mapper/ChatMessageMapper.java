package com.biliplus.mapper;

import com.biliplus.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void insert(ChatMessage message);

    ChatMessage selectById(Long id);

    List<ChatMessage> selectMessageByConversationId(
            @Param("conversationId") Long convId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );
}

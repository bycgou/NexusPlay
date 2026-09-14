package com.biliplus.mapper;

import com.biliplus.pojo.dto.userdto.ChatConversationDTO;
import com.biliplus.pojo.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatConversationMapper {
    ChatConversation selectById(@Param("id") Long id);

    ChatConversation findPrivateBetween(Long userid, Long targetUserId);

    // 创建会话
    void insertPrivate(ChatConversation conv);

    List<ChatConversationDTO> selectByIds(Long userid);
}

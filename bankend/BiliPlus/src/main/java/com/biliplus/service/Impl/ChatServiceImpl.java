package com.biliplus.service.Impl;

import com.biliplus.mapper.ChatConversationMapper;
import com.biliplus.mapper.ChatConversationMemberMapper;
import com.biliplus.mapper.ChatMessageMapper;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.pojo.dto.ChatMessageDTO;
import com.biliplus.pojo.dto.userdto.ChatConversationDTO;
import com.biliplus.pojo.entity.ChatConversation;
import com.biliplus.pojo.entity.ChatConversationMember;
import com.biliplus.pojo.entity.ChatMessage;
import com.biliplus.pojo.entity.User;
import com.biliplus.service.ChatService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Builder
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private  ChatMessageMapper messageMapper;
    @Autowired
    private  ChatConversationMemberMapper memberMapper;
    @Autowired
    private  ChatConversationMapper conversationMapper;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

//    @Override
//    @Transactional
//    public ChatMessage saveMessage(Long senderId, ChatMessageDTO dto) {
//        // 1. 校验用户是否在会话中
//        if (!isUserInConversation(senderId, dto.getConversationId())) {
//            throw new IllegalArgumentException("用户无权在此会话中发送消息");
//        }
//
//        // 2. 保存消息
//        ChatMessage msg = new ChatMessage();
//        msg.setConversationId(dto.getConversationId());
//        msg.setSenderId(senderId);
//        msg.setContent(dto.getContent());
//        msg.setMsgType(dto.getMsgType().byteValue());
//        msg.setCreateTime(LocalDateTime.now());
//        msg.setUpdateTime(LocalDateTime.now());
//        messageMapper.insert(msg); // ID 自动回填
//
//        // 3. 给其他成员增加未读数
//        List<Long> receiverIds = memberMapper.selectOtherMemberUserIds(dto.getConversationId(), senderId);
//        for (Long uid : receiverIds) {
//            memberMapper.incrementUnreadCount(dto.getConversationId(), uid);
//        }
//
//        return msg;
//    }

    @Override
    public boolean isUserInConversation(Long userId, Long conversationId) {
        return memberMapper.countActiveMember(conversationId, userId) > 0;
    }

    @Override
    public Iterable<Long> getReceiverUserIds(Long conversationId, Long senderId) {
        return memberMapper.selectOtherMemberUserIds(conversationId, senderId);
    }


    @Override
    public ChatConversationDTO.UserVO getOtherUser(Long targetUserId) {

        User  user = peopleUserMapper.getUserById(targetUserId);
        return ChatConversationDTO.UserVO.builder()
                .id(user.getId())
                .name(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }


    // 创建会话
    @Override
    public ChatConversation createConversations(Long userid , Long targetUserId) {
        log. info("创建会话,当前用户id:{},会话目标用户id:{}", userid,targetUserId);
        if(userid.equals(targetUserId)){
            throw new IllegalArgumentException("不能创建与自己对话的会话");
        }

        // 查对话是否存在
        ChatConversation conversation = conversationMapper.findPrivateBetween(userid, targetUserId);
        if(conversation != null){
            return conversation;
        }
        // 对话不存在,创建新对话
        ChatConversation conv =  ChatConversation.builder()
                .type(( byte)1)
                .name(null)
                .avatar(null)
                .createTime(LocalDateTime.now())
                .build();
        conversationMapper.insertPrivate(conv);

        // 判断是否获取自增
        if(conv.getId() == null){
            log.error("创建会话失败,getID为空--->conv的值为{}",conv);
            throw new RuntimeException("创建会话失败,getID为空");
        }

        // 添加双方成员
        // 添加---->发起会话成员
        memberMapper.insert(ChatConversationMember.builder()
                .conversationId(conv.getId())
                .userId(userid)
                .unreadCount(0)
                .lastReadMsgId(null)
                .joinTime(LocalDateTime.now())
                .build()

        );
        // 添加---->目标会话成员
        memberMapper.insert(ChatConversationMember.builder()
                .conversationId(conv.getId())
                .userId(targetUserId)
                .unreadCount(0)
                .lastReadMsgId(null)
                .joinTime(LocalDateTime.now())
                .build()
        );

        return conv;
    }


    // 获取会话列表
    @Override
    public List<ChatConversationDTO> getConversationList(Long userid) {
        log.info("--获取会话列表--Service层");
        return conversationMapper.selectByIds(userid);
    }

    // 获取会话消息（分页）
    @Override
    public List<ChatMessage> getMessage(Long convId, int page, int pageSize, Long userId) {
        log.info("--获取会话消息--Service层, convId={}, page={}, pageSize={}", convId, page, pageSize);
        if (userId == null || !isUserInConversation(userId, convId)) {
            throw new IllegalArgumentException("无权查看该会话消息");
        }
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 50;
        int offset = (page - 1) * pageSize;
        return messageMapper.selectMessageByConversationId(convId, offset, pageSize);
    }
    //=========================websocket========================================

    @Override
    @Transactional
    public ChatMessage saveMessage(Long senderId, Long conversationId, String content, Byte msgType) {
        log.info("保存消息,会话id:{},发送者id:{},内容:{},消息类型:{}", conversationId,senderId,content,msgType);
        if (!isUserInConversation(senderId, conversationId)) {
            throw new IllegalArgumentException("用户无权在此会话中发送消息");
        }
        // 保存消息
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setMsgType( msgType);
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        messageMapper.insert( msg); // ID 自动回填

        // 查找对方UserId(私聊)
        Long receiverId = memberMapper.selectOtherMemberUserId(conversationId, senderId);
        if(receiverId !=null){
            memberMapper.incrementUnreadCount(conversationId, receiverId);
        }
        return msg;
    }

    // 获取对方UserId(私聊)
    @Override
    public Long selectOtherUserIdInPrivate(Long conversationId, Long senderId) {

        return memberMapper.selectOtherMemberUserId(conversationId, senderId);
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        log.info("标记会话已读: conversationId={}, userId={}", conversationId, userId);
        if (userId == null || !isUserInConversation(userId, conversationId)) {
            throw new IllegalArgumentException("无权操作该会话");
        }

        // 1. 获取该会话中最新的消息ID（查询最近1条）
        List<ChatMessage> messages = messageMapper.selectMessageByConversationId(conversationId, 0, 1);
        if (messages == null || messages.isEmpty()) {
            log.info("会话无消息，无需标记已读: conversationId={}", conversationId);
            return;
        }

        // 获取最新消息ID（查询结果按时间降序，第一条即最新）
        Long latestMsgId = messages.get(0).getId();

        // 2. 更新 lastReadMsgId 并清零 unread_count（updateLastReadMsgId 内部已包含 unread_count=0）
        memberMapper.updateLastReadMsgId(conversationId, userId, latestMsgId);

        log.info("会话已标记已读: conversationId={}, userId={}, lastMsgId={}", conversationId, userId, latestMsgId);
    }

    @Override
    public int countUnread(Long userId) {
        if (userId == null) {
            return 0;
        }
        return memberMapper.sumUnreadCount(userId);
    }
}

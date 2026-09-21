package com.biliplus.service.Impl;

import com.biliplus.constant.Notify;
import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.NotificationMapper;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.pojo.entity.Notification;
import com.biliplus.result.PageResult;
import com.biliplus.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    @Override
    public void notify(Long toUserId, Long fromUserId, int type, String title, String content,
                       String bizType, Long bizId) {
        if (toUserId == null || !StringUtils.hasText(title)) {
            return;
        }
        // 自己触发的事件不通知自己
        if (fromUserId != null && Objects.equals(toUserId, fromUserId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(toUserId);
        notification.setType(type);
        notification.setTitle(truncate(title, 100));
        notification.setContent(truncate(content, 500));
        notification.setBizType(bizType);
        notification.setBizId(bizId);
        notification.setFromUserId(fromUserId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        try {
            notificationMapper.insert(notification);
        } catch (Exception e) {
            // 通知属于旁路逻辑，不能因为写通知失败而回滚主业务
            log.warn("写入通知失败 toUserId={}, type={}", toUserId, type, e);
        }
    }

    @Override
    @Transactional
    public void notifyAll(String title, String content) {
        if (!StringUtils.hasText(title)) {
            throw new BusinessException("通知标题不能为空");
        }
        List<Long> userIds = peopleUserMapper.selectAllActiveUserIds();
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(Notify.TYPE_SYSTEM);
            notification.setTitle(truncate(title, 100));
            notification.setContent(truncate(content, 500));
            notification.setBizType(Notify.BIZ_SYSTEM);
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
        }
        log.info("已向 {} 位用户发送系统通知：{}", userIds.size(), title);
    }

    @Override
    public PageResult list(Long userId, Integer isRead, Integer type, Integer page, Integer size) {
        requireLogin(userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        List<Notification> records = notificationMapper
                .pageByUser(userId, isRead, type, (p - 1) * s, s);
        long total = notificationMapper.countByUser(userId, isRead, type);
        return new PageResult(total, records);
    }

    @Override
    public long unreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markRead(Long userId, Long id) {
        requireLogin(userId);
        if (id == null) {
            throw new BusinessException("通知ID不能为空");
        }
        int rows = notificationMapper.markRead(id, userId);
        if (rows <= 0) {
            throw new BusinessException("通知不存在");
        }
    }

    @Override
    public void markAllRead(Long userId) {
        requireLogin(userId);
        notificationMapper.markAllRead(userId);
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}

package com.biliplus.service;

import com.biliplus.result.PageResult;

public interface NotificationService {

    /**
     * 写一条通知。
     * 自己触发自己的事件（如自己评论自己的视频）不产生通知，避免通知风暴。
     */
    void notify(Long toUserId, Long fromUserId, int type, String title, String content,
                String bizType, Long bizId);

    /** 管理端发送系统通知 */
    void notifyAll(String title, String content);

    PageResult list(Long userId, Integer isRead, Integer type, Integer page, Integer size);

    long unreadCount(Long userId);

    void markRead(Long userId, Long id);

    void markAllRead(Long userId);
}

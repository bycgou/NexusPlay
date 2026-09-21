package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 站内通知 */
@Data
public class Notification {

    private Long id;
    /** 接收者 */
    private Long userId;
    /** 1评论 2回复 3关注 4审核 5直播开播 6系统 */
    private Integer type;
    private String title;
    private String content;
    /** video|comment|live|system */
    private String bizType;
    private Long bizId;
    /** 触发者 */
    private Long fromUserId;
    /** 0未读 1已读 */
    private Integer isRead;
    private LocalDateTime createTime;
}

package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播观众表实体类
 * 对应数据库表：live_user
 */
@Data
public class LiveUser {

    /** ID（自增主键） */
    private Long id;

    /** 直播间ID */
    private Long liveRoomId;

    /** 用户ID */
    private Long userId;

    /** 进入时间 */
    private LocalDateTime enterTime;

    /** 离开时间 */
    private LocalDateTime leaveTime;
}

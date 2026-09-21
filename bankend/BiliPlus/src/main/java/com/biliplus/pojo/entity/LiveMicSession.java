package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 连麦会话表 */
@Data
public class LiveMicSession {
    private Long id;
    private Long liveRoomId;
    private Long hostUserId;
    private Long guestUserId;
    /** 0申请 1进行中 2结束 3拒绝 */
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}

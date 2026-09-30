package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户处置记录。endTime 为 null 表示永久 */
@Data
public class UserPenalty {

    private Long id;
    private Long userId;
    /** mute 禁言 / ban 封禁 */
    private String action;
    private String reason;
    private LocalDateTime startTime;
    /** 到期时间，null=永久 */
    private LocalDateTime endTime;
    private Long adminId;
    /** 1生效中 2已到期 3已提前解除 */
    private Integer status;
    private LocalDateTime createTime;
}

package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户信用分。初始 100，低于阈值触发自动禁言 */
@Data
public class UserCredit {

    private Long userId;
    private Integer score;
    private Integer violationCount;
    private LocalDateTime lastViolationTime;
    private LocalDateTime updateTime;
}

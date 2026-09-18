package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户钱包（硬币） */
@Data
public class UserWallet {
    private Long userId;
    private Long balance;
    private Integer version;
    private LocalDateTime updateTime;
}

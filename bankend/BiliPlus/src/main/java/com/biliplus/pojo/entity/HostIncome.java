package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 主播收益汇总 */
@Data
public class HostIncome {
    private Long userId;
    private Long totalIncome;
    private LocalDateTime updateTime;
}

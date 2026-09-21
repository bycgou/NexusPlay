package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 钱包账变流水。
 * balanceAfter 的口径由 bizType 决定：recharge/gift/adjust 为 user_wallet.balance，
 * host_income 为 host_income.total_income。
 */
@Data
public class WalletTransaction {

    private Long id;
    private Long userId;
    /** 1充值 2送礼支出 3主播收入 4系统调整 */
    private Integer type;
    /** 变动金额，正为入账，负为出账 */
    private Long amount;
    private Long balanceAfter;
    /** recharge|gift|host_income|adjust */
    private String bizType;
    /** 关联业务ID：充值订单ID / 打赏流水ID */
    private Long bizId;
    private String remark;
    private LocalDateTime createTime;
}

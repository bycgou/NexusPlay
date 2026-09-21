package com.biliplus.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 充值订单。本期为模拟支付，仅预留真实支付网关所需的订单结构 */
@Data
public class RechargeOrder {

    private Long id;
    private String orderNo;
    private Long userId;
    /** 硬币数量 */
    private Long amount;
    /** 应付金额 */
    private BigDecimal payAmount;
    /** 0待支付 1已支付 2已取消 */
    private Integer status;
    /** mock|alipay|wechat */
    private String payChannel;
    private LocalDateTime paidTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

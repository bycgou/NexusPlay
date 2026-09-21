package com.biliplus.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 充值订单对外视图 */
@Data
public class RechargeOrderVO {

    private String orderNo;
    private Long amount;
    private BigDecimal payAmount;
    /** 0待支付 1已支付 2已取消 */
    private Integer status;
    private LocalDateTime paidTime;
    private LocalDateTime createTime;
}

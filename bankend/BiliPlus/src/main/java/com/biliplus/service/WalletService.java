package com.biliplus.service;

import com.biliplus.pojo.vo.RechargeOrderVO;
import com.biliplus.result.PageResult;

public interface WalletService {

    /** 创建充值订单（待支付），返回订单号与应付金额 */
    RechargeOrderVO createRechargeOrder(Long userId, Long amount);

    /** 模拟支付：订单置为已支付、余额入账并写充值账变 */
    RechargeOrderVO payRechargeOrder(Long userId, String orderNo);

    /** 我的账变流水（分页，可按类型筛选） */
    PageResult myTransactions(Long userId, Integer type, Integer page, Integer size);

    /** 管理端对账查询 */
    PageResult adminTransactions(Long userId, Integer type, String bizType,
                                 String from, String to, Integer page, Integer size);

    /**
     * 记一笔账变。
     * 供送礼等业务在自身事务内调用，保证余额变动与账变流水同成同败；
     * balanceAfter 的余额口径由 bizType 决定，见 {@link com.biliplus.pojo.entity.WalletTransaction}。
     */
    void insertTransaction(Long userId, int type, long amount, long balanceAfter,
                           String bizType, Long bizId, String remark);
}

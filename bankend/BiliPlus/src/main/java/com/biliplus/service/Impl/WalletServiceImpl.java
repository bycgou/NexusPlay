package com.biliplus.service.Impl;

import com.biliplus.constant.WalletTx;
import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.RechargeOrderMapper;
import com.biliplus.mapper.UserWalletMapper;
import com.biliplus.mapper.WalletTransactionMapper;
import com.biliplus.pojo.entity.RechargeOrder;
import com.biliplus.pojo.entity.UserWallet;
import com.biliplus.pojo.entity.WalletTransaction;
import com.biliplus.pojo.vo.RechargeOrderVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    /** 账变类型 */
    public static final int TYPE_RECHARGE = WalletTx.TYPE_RECHARGE;
    public static final int TYPE_GIFT = WalletTx.TYPE_GIFT;
    public static final int TYPE_HOST_INCOME = WalletTx.TYPE_HOST_INCOME;
    public static final int TYPE_ADJUST = WalletTx.TYPE_ADJUST;

    /** 业务类型 */
    public static final String BIZ_RECHARGE = WalletTx.BIZ_RECHARGE;
    public static final String BIZ_GIFT = WalletTx.BIZ_GIFT;
    public static final String BIZ_HOST_INCOME = WalletTx.BIZ_HOST_INCOME;

    /** 模拟支付渠道；接入真实网关后由回调写入 */
    private static final String CHANNEL_MOCK = "mock";

    /** 1 硬币 = 0.01 元，与硬币面额口径保持一致 */
    private static final BigDecimal COIN_UNIT_PRICE = new BigDecimal("0.01");

    private static final long MIN_RECHARGE_AMOUNT = 1L;
    private static final long MAX_RECHARGE_AMOUNT = 1_000_000L;

    private static final DateTimeFormatter ORDER_NO_TIME =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private UserWalletMapper userWalletMapper;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private RechargeOrderMapper rechargeOrderMapper;

    @Override
    @Transactional
    public RechargeOrderVO createRechargeOrder(Long userId, Long amount) {
        requireLogin(userId);
        validateAmount(amount);

        LocalDateTime now = LocalDateTime.now();
        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAmount(amount);
        order.setPayAmount(coinToPrice(amount));
        order.setStatus(0);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        rechargeOrderMapper.insert(order);

        log.info("创建充值订单 userId={}, orderNo={}, amount={}", userId, order.getOrderNo(), amount);
        return toVO(order);
    }

    @Override
    @Transactional
    public RechargeOrderVO payRechargeOrder(Long userId, String orderNo) {
        requireLogin(userId);
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException("订单号不能为空");
        }
        RechargeOrder order = rechargeOrderMapper.selectByOrderNo(orderNo.trim());
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            throw new BusinessException("订单已支付，请勿重复提交");
        }
        if (order.getStatus() != null && order.getStatus() != 0) {
            throw new BusinessException("订单状态不可支付");
        }

        // 先占用订单，避免并发重复入账
        LocalDateTime paidTime = LocalDateTime.now();
        int rows = rechargeOrderMapper.markPaid(order.getOrderNo(), userId, CHANNEL_MOCK, paidTime);
        if (rows <= 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }

        userWalletMapper.ensureWallet(userId);
        userWalletMapper.recharge(userId, order.getAmount());
        UserWallet wallet = userWalletMapper.selectByUserId(userId);
        long balanceAfter = wallet == null || wallet.getBalance() == null ? 0L : wallet.getBalance();

        insertTransaction(userId, TYPE_RECHARGE, order.getAmount(), balanceAfter,
                BIZ_RECHARGE, order.getId(), "充值订单 " + order.getOrderNo());

        order.setStatus(1);
        order.setPayChannel(CHANNEL_MOCK);
        order.setPaidTime(paidTime);
        log.info("充值订单支付成功 userId={}, orderNo={}, balanceAfter={}",
                userId, order.getOrderNo(), balanceAfter);
        return toVO(order);
    }

    @Override
    public PageResult myTransactions(Long userId, Integer type, Integer page, Integer size) {
        requireLogin(userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        List<WalletTransaction> records = walletTransactionMapper
                .pageByUser(userId, type, (p - 1) * s, s);
        long total = walletTransactionMapper.countByUser(userId, type);
        return new PageResult(total, records);
    }

    @Override
    public PageResult adminTransactions(Long userId, Integer type, String bizType,
                                        String from, String to, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        String fromTime = normalizeTime(from, false);
        String toTime = normalizeTime(to, true);
        List<WalletTransaction> records = walletTransactionMapper
                .adminList(userId, type, bizType, fromTime, toTime, (p - 1) * s, s);
        long total = walletTransactionMapper.adminCount(userId, type, bizType, fromTime, toTime);
        return new PageResult(total, records);
    }

    /** 记一笔账变；只在本类与 GiftServiceImpl 的同一事务内调用 */
    @Override
    public void insertTransaction(Long userId, int type, long amount, long balanceAfter,
                                  String bizType, Long bizId, String remark) {
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setBizType(bizType);
        tx.setBizId(bizId);
        tx.setRemark(remark);
        tx.setCreateTime(LocalDateTime.now());
        walletTransactionMapper.insert(tx);
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount < MIN_RECHARGE_AMOUNT || amount > MAX_RECHARGE_AMOUNT) {
            throw new BusinessException("充值数量必须在 " + MIN_RECHARGE_AMOUNT + "-" + MAX_RECHARGE_AMOUNT + " 之间");
        }
    }

    private BigDecimal coinToPrice(long amount) {
        return COIN_UNIT_PRICE.multiply(BigDecimal.valueOf(amount));
    }

    private String generateOrderNo() {
        return "R" + LocalDateTime.now().format(ORDER_NO_TIME)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    /** 管理端按天筛选时，end 需要包含当天整天 */
    private String normalizeTime(String value, boolean endOfDay) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 10) {
            return trimmed + (endOfDay ? " 23:59:59" : " 00:00:00");
        }
        return trimmed;
    }

    private RechargeOrderVO toVO(RechargeOrder order) {
        RechargeOrderVO vo = new RechargeOrderVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setAmount(order.getAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setPaidTime(order.getPaidTime());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }
}

package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.RechargeOrderMapper;
import com.biliplus.mapper.UserWalletMapper;
import com.biliplus.mapper.WalletTransactionMapper;
import com.biliplus.pojo.entity.RechargeOrder;
import com.biliplus.pojo.entity.UserWallet;
import com.biliplus.pojo.entity.WalletTransaction;
import com.biliplus.pojo.vo.RechargeOrderVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.Impl.WalletServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private UserWalletMapper userWalletMapper;

    @Mock
    private WalletTransactionMapper walletTransactionMapper;

    @Mock
    private RechargeOrderMapper rechargeOrderMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    private RechargeOrder order(long id, long amount, int status) {
        RechargeOrder o = new RechargeOrder();
        o.setId(id);
        o.setOrderNo("R20260101000000123456");
        o.setUserId(9L);
        o.setAmount(amount);
        o.setPayAmount(new BigDecimal("1.00"));
        o.setStatus(status);
        return o;
    }

    @Test
    void createOrder_whenNotLogin_shouldThrow() {
        assertThrows(BusinessException.class, () -> walletService.createRechargeOrder(null, 100L));
    }

    @Test
    void createOrder_whenAmountOutOfRange_shouldThrow() {
        assertThrows(BusinessException.class, () -> walletService.createRechargeOrder(9L, 0L));
        assertThrows(BusinessException.class, () -> walletService.createRechargeOrder(9L, 1_000_001L));
        verify(rechargeOrderMapper, never()).insert(any());
    }

    @Test
    void createOrder_shouldReturnOrderNoAndPayAmount() {
        doAnswer(inv -> {
            ((RechargeOrder) inv.getArgument(0)).setId(77L);
            return null;
        }).when(rechargeOrderMapper).insert(any(RechargeOrder.class));

        RechargeOrderVO vo = walletService.createRechargeOrder(9L, 100L);

        assertNotNull(vo.getOrderNo());
        assertTrue(vo.getOrderNo().startsWith("R"));
        assertEquals(100L, vo.getAmount());
        // 1 硬币 = 0.01 元
        assertEquals(0, new BigDecimal("1.00").compareTo(vo.getPayAmount()));
        assertEquals(0, vo.getStatus());
    }

    @Test
    void pay_whenOrderBelongsToOtherUser_shouldThrow() {
        RechargeOrder other = order(77L, 100L, 0);
        other.setUserId(88L);
        when(rechargeOrderMapper.selectByOrderNo("R20260101000000123456")).thenReturn(other);

        assertThrows(BusinessException.class,
                () -> walletService.payRechargeOrder(9L, "R20260101000000123456"));
        verify(userWalletMapper, never()).recharge(anyLong(), anyLong());
    }

    @Test
    void pay_whenAlreadyPaid_shouldThrow() {
        when(rechargeOrderMapper.selectByOrderNo("R20260101000000123456"))
                .thenReturn(order(77L, 100L, 1));

        assertThrows(BusinessException.class,
                () -> walletService.payRechargeOrder(9L, "R20260101000000123456"));
    }

    @Test
    void pay_shouldRechargeAndWriteLedger() {
        when(rechargeOrderMapper.selectByOrderNo("R20260101000000123456"))
                .thenReturn(order(77L, 100L, 0));
        when(rechargeOrderMapper.markPaid(any(), anyLong(), any(), any())).thenReturn(1);
        UserWallet wallet = new UserWallet();
        wallet.setUserId(9L);
        wallet.setBalance(500L);
        when(userWalletMapper.selectByUserId(9L)).thenReturn(wallet);

        RechargeOrderVO vo = walletService.payRechargeOrder(9L, "R20260101000000123456");

        assertEquals(1, vo.getStatus());
        verify(userWalletMapper).ensureWallet(9L);
        verify(userWalletMapper).recharge(9L, 100L);

        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionMapper).insert(captor.capture());
        WalletTransaction tx = captor.getValue();
        assertEquals(9L, tx.getUserId());
        assertEquals(1, tx.getType());
        assertEquals(100L, tx.getAmount());
        assertEquals(500L, tx.getBalanceAfter());
        assertEquals("recharge", tx.getBizType());
        assertEquals(77L, tx.getBizId());
    }

    @Test
    void pay_whenMarkPaidLosesRace_shouldNotRecharge() {
        when(rechargeOrderMapper.selectByOrderNo("R20260101000000123456"))
                .thenReturn(order(77L, 100L, 0));
        when(rechargeOrderMapper.markPaid(any(), anyLong(), any(), any())).thenReturn(0);

        assertThrows(BusinessException.class,
                () -> walletService.payRechargeOrder(9L, "R20260101000000123456"));
        verify(userWalletMapper, never()).recharge(anyLong(), anyLong());
        verify(walletTransactionMapper, never()).insert(any());
    }

    @Test
    void myTransactions_whenNotLogin_shouldThrow() {
        assertThrows(BusinessException.class, () -> walletService.myTransactions(null, null, 1, 20));
    }

    @Test
    void myTransactions_shouldPage() {
        when(walletTransactionMapper.pageByUser(9L, null, 0, 20)).thenReturn(List.of(new WalletTransaction()));
        when(walletTransactionMapper.countByUser(9L, null)).thenReturn(1L);

        PageResult result = walletService.myTransactions(9L, null, 1, 20);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void adminTransactions_shouldPassDateRangeThrough() {
        when(walletTransactionMapper.adminList(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(walletTransactionMapper.adminCount(any(), any(), any(), any(), any())).thenReturn(0L);

        walletService.adminTransactions(9L, 2, "gift", "2026-01-01", "2026-01-02", 1, 20);

        // 按天筛选时结束日期需要补到当天 23:59:59
        verify(walletTransactionMapper).adminList(9L, 2, "gift",
                "2026-01-01 00:00:00", "2026-01-02 23:59:59", 0, 20);
    }
}

package com.biliplus.controller.user;

import com.biliplus.pojo.vo.RechargeOrderVO;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.WalletService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pp/live/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /** 创建充值订单，返回 orderNo 与应付金额 */
    @PostMapping("/recharge/orders")
    public Result<RechargeOrderVO> createOrder(@RequestBody Map<String, Long> body) {
        try {
            Long userId = UserContext.getCurrentUserId();
            Long amount = body == null ? null : body.get("amount");
            return Result.success(walletService.createRechargeOrder(userId, amount));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 模拟支付：本期不接真实网关，调用即视为支付成功 */
    @PostMapping("/recharge/orders/{orderNo}/pay")
    public Result<RechargeOrderVO> payOrder(@PathVariable String orderNo) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(walletService.payRechargeOrder(userId, orderNo));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 我的账变流水 */
    @GetMapping("/transactions")
    public Result<PageResult> transactions(@RequestParam(required = false) Integer type,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "20") Integer size) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(walletService.myTransactions(userId, type, page, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

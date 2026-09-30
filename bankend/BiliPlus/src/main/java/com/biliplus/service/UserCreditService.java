package com.biliplus.service;

import com.biliplus.pojo.entity.UserCredit;

public interface UserCreditService {

    /** 保证信用记录存在，新用户默认 100 分 */
    void ensure(Long userId);

    UserCredit get(Long userId);

    /**
     * 记一次违规并扣分。
     * 低于阈值时自动追加禁言处置：< 60 禁言 7 天，< 30 禁言 30 天。
     */
    void applyViolation(Long userId, int delta, String reason);

    /** 正常行为加分（不计违规） */
    void applyReward(Long userId, int delta);
}

package com.biliplus.service.Impl;

import com.biliplus.mapper.UserCreditMapper;
import com.biliplus.pojo.entity.UserCredit;
import com.biliplus.service.UserCreditService;
import com.biliplus.service.UserPenaltyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserCreditServiceImpl implements UserCreditService {

    public static final int INITIAL_SCORE = 100;
    /** 低于该分自动禁言 7 天 */
    public static final int THRESHOLD_SHORT = 60;
    /** 低于该分自动禁言 30 天 */
    public static final int THRESHOLD_LONG = 30;

    @Autowired
    private UserCreditMapper userCreditMapper;

    @Autowired
    private UserPenaltyService userPenaltyService;

    @Override
    public void ensure(Long userId) {
        if (userId == null) {
            return;
        }
        userCreditMapper.ensure(userId);
    }

    @Override
    public UserCredit get(Long userId) {
        if (userId == null) {
            return null;
        }
        ensure(userId);
        return userCreditMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void applyViolation(Long userId, int delta, String reason) {
        if (userId == null) {
            return;
        }
        ensure(userId);
        userCreditMapper.changeScore(userId, Math.min(0, delta), 1);

        UserCredit credit = userCreditMapper.selectByUserId(userId);
        int score = credit == null || credit.getScore() == null ? INITIAL_SCORE : credit.getScore();
        log.info("信用扣分 userId={}, delta={}, 剩余={}, 原因={}", userId, delta, score, reason);

        // 低于阈值自动追加禁言；已有生效中的禁言时不重复处罚
        if (score < THRESHOLD_LONG) {
            autoPenalize(userId, 30, "信用分低于 " + THRESHOLD_LONG + "：" + reason);
        } else if (score < THRESHOLD_SHORT) {
            autoPenalize(userId, 7, "信用分低于 " + THRESHOLD_SHORT + "：" + reason);
        }
    }

    @Override
    @Transactional
    public void applyReward(Long userId, int delta) {
        if (userId == null || delta <= 0) {
            return;
        }
        ensure(userId);
        userCreditMapper.changeScore(userId, delta, 0);
    }

    private void autoPenalize(Long userId, int days, String reason) {
        try {
            userPenaltyService.penalize(userId, "mute", reason, days, null);
        } catch (Exception e) {
            // 自动禁言失败不能影响扣分主流程
            log.warn("信用分触发自动禁言失败 userId={}", userId, e);
        }
    }
}

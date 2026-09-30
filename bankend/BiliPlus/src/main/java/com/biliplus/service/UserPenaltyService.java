package com.biliplus.service;

import com.biliplus.pojo.entity.UserPenalty;
import com.biliplus.result.PageResult;

public interface UserPenaltyService {

    /** days 为 null 表示永久 */
    UserPenalty penalize(Long userId, String action, String reason, Integer days, Long adminId);

    /** 提前解除该用户生效中的同类处置 */
    void release(Long userId, String action, Long adminId);

    boolean isMuted(Long userId);

    boolean isBanned(Long userId);

    PageResult adminList(Long userId, Integer status, Integer page, Integer size);

    /** 到期处置自动结束，由定时任务调用 */
    int expireDue();
}

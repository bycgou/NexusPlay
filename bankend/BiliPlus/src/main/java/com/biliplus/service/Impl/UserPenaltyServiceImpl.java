package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.mapper.UserPenaltyMapper;
import com.biliplus.pojo.entity.UserPenalty;
import com.biliplus.result.PageResult;
import com.biliplus.service.UserPenaltyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserPenaltyServiceImpl implements UserPenaltyService {

    public static final String ACTION_MUTE = "mute";
    public static final String ACTION_BAN = "ban";

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_EXPIRED = 2;
    public static final int STATUS_RELEASED = 3;

    @Autowired
    private UserPenaltyMapper userPenaltyMapper;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    @Autowired
    private com.biliplus.service.EventLogService eventLogService;

    @Autowired
    private com.biliplus.service.AdminOperationLogService adminOperationLogService;

    @Override
    @Transactional
    public UserPenalty penalize(Long userId, String action, String reason, Integer days, Long adminId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        String normalized = normalizeAction(action);
        if (userPenaltyMapper.countActive(userId, normalized) > 0) {
            throw new BusinessException("该用户已有生效中的" + (ACTION_MUTE.equals(normalized) ? "禁言" : "封禁"));
        }

        LocalDateTime now = LocalDateTime.now();
        UserPenalty penalty = new UserPenalty();
        penalty.setUserId(userId);
        penalty.setAction(normalized);
        penalty.setReason(StringUtils.hasText(reason) ? reason.trim() : null);
        penalty.setStartTime(now);
        penalty.setEndTime(days == null ? null : now.plusDays(days));
        penalty.setAdminId(adminId);
        penalty.setStatus(STATUS_ACTIVE);
        penalty.setCreateTime(now);
        userPenaltyMapper.insert(penalty);

        // 封禁同步 user.status，使 C 端登录/鉴权立即失效；禁言不影响登录
        if (ACTION_BAN.equals(normalized)) {
            peopleUserMapper.updateUserStatus(userId, 0);
        }
        log.info("用户处置 userId={}, action={}, days={}, adminId={}", userId, normalized, days, adminId);
        eventLogService.record(com.biliplus.constant.EventType.USER_BAN, userId,
                com.biliplus.constant.EventType.TARGET_USER, userId,
                null, "{\"action\":\"" + normalized + "\",\"days\":" + (days == null ? "null" : days) + "}",
                adminId == null ? com.biliplus.constant.EventType.SOURCE_SYSTEM
                        : com.biliplus.constant.EventType.SOURCE_ADMIN);
        // 信用分自动触发时 adminId 为空，仍要留痕以便排查自动处罚
        adminOperationLogService.record(
                adminId == null ? "user.penalty.auto" : "user.penalty." + normalized,
                "user", userId,
                normalized + " " + (days == null ? "永久" : days + "天")
                        + (reason == null ? "" : "，" + reason));
        return penalty;
    }

    @Override
    @Transactional
    public void release(Long userId, String action, Long adminId) {
        String normalized = normalizeAction(action);
        int rows = userPenaltyMapper.releaseActive(userId, normalized);
        if (rows <= 0) {
            throw new BusinessException("该用户没有生效中的此类处置");
        }
        if (ACTION_BAN.equals(normalized)) {
            peopleUserMapper.updateUserStatus(userId, 1);
        }
        log.info("提前解除处置 userId={}, action={}, adminId={}", userId, normalized, adminId);
    }

    @Override
    public boolean isMuted(Long userId) {
        return userId != null && userPenaltyMapper.countActive(userId, ACTION_MUTE) > 0;
    }

    @Override
    public boolean isBanned(Long userId) {
        return userId != null && userPenaltyMapper.countActive(userId, ACTION_BAN) > 0;
    }

    @Override
    public PageResult adminList(Long userId, Integer status, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        List<UserPenalty> records = userPenaltyMapper.adminList(userId, status, (p - 1) * s, s);
        return new PageResult(userPenaltyMapper.adminCount(userId, status), records);
    }

    @Override
    @Transactional
    public int expireDue() {
        List<UserPenalty> due = userPenaltyMapper.selectExpired(LocalDateTime.now());
        if (due == null || due.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (UserPenalty penalty : due) {
            int rows = userPenaltyMapper.changeStatus(penalty.getId(), STATUS_EXPIRED);
            if (rows > 0) {
                count++;
                if (ACTION_BAN.equals(penalty.getAction())) {
                    // 封禁到期恢复账号状态
                    peopleUserMapper.updateUserStatus(penalty.getUserId(), 1);
                }
            }
        }
        if (count > 0) {
            log.info("处置到期自动解除 {} 条", count);
        }
        return count;
    }

    /** 每分钟扫一次到期处置，保证禁言/封禁按时失效 */
    @Scheduled(fixedDelay = 60_000L, initialDelay = 30_000L)
    public void scheduledExpire() {
        try {
            expireDue();
        } catch (Exception e) {
            log.warn("到期处置扫描失败", e);
        }
    }

    private String normalizeAction(String action) {
        if (ACTION_BAN.equalsIgnoreCase(action)) {
            return ACTION_BAN;
        }
        return ACTION_MUTE;
    }
}

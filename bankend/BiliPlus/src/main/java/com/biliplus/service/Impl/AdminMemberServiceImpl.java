package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.AdminMemberMapper;
import com.biliplus.pojo.entity.UserPenalty;
import com.biliplus.pojo.vo.AdminMemberVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.AdminMemberService;
import com.biliplus.service.UserPenaltyService;
import com.biliplus.service.Impl.UserPenaltyServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminMemberServiceImpl implements AdminMemberService {

    public static final int ROLE_NORMAL = 0;
    public static final int ROLE_UP = 1;
    public static final int ROLE_ADMIN = 2;

    @Autowired
    private AdminMemberMapper adminMemberMapper;

    @Autowired
    private UserPenaltyService userPenaltyService;

    @Autowired
    private com.biliplus.service.AdminOperationLogService adminOperationLogService;

    @Override
    public PageResult list(String keyword, Integer status, Integer role, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        String trimmed = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Long keywordId = parseId(trimmed);
        String likeKeyword = trimmed == null ? null : com.biliplus.utils.LikeEscape.escape(trimmed);
        List<AdminMemberVO> records = adminMemberMapper.adminList(
                likeKeyword, keywordId, status, role, (p - 1) * s, s);
        long total = adminMemberMapper.adminCount(likeKeyword, keywordId, status, role);
        return new PageResult(total, records);
    }

    @Override
    public AdminMemberVO detail(Long id) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        AdminMemberVO vo = adminMemberMapper.selectDetail(id);
        if (vo == null) {
            throw new BusinessException("用户不存在");
        }
        return vo;
    }

    @Override
    public void ban(Long userId, String reason, Integer days, Long adminId) {
        detail(userId);
        userPenaltyService.penalize(userId, UserPenaltyServiceImpl.ACTION_BAN,
                StringUtils.hasText(reason) ? reason.trim() : "管理员封禁",
                days, adminId);
    }

    @Override
    public void unban(Long userId, Long adminId) {
        detail(userId);
        userPenaltyService.release(userId, UserPenaltyServiceImpl.ACTION_BAN, adminId);
    }

    @Override
    public void mute(Long userId, String reason, Integer days, Long adminId) {
        detail(userId);
        userPenaltyService.penalize(userId, UserPenaltyServiceImpl.ACTION_MUTE,
                StringUtils.hasText(reason) ? reason.trim() : "管理员禁言",
                days, adminId);
    }

    @Override
    public void unmute(Long userId, Long adminId) {
        detail(userId);
        userPenaltyService.release(userId, UserPenaltyServiceImpl.ACTION_MUTE, adminId);
    }

    @Override
    public void updateRole(Long userId, Integer role) {
        detail(userId);
        if (role == null || role < ROLE_NORMAL || role > ROLE_ADMIN) {
            throw new BusinessException("角色取值非法（0普通/1UP/2管理员）");
        }
        adminMemberMapper.updateRole(userId, role);
        log.info("调整用户角色 userId={}, role={}", userId, role);
        // 封禁/禁言已在 UserPenaltyService 留痕，角色调整是独立动作需单独记
        adminOperationLogService.record("user.role", "user", userId, "role=" + role);
    }

    @Override
    public Map<String, Object> penalties(Long userId) {
        detail(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("activeMute", userPenaltyService.isMuted(userId));
        result.put("activeBan", userPenaltyService.isBanned(userId));
        result.put("list", userPenaltyService.adminList(userId, null, 1, 50));
        return result;
    }

    private Long parseId(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

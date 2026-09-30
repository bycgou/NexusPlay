package com.biliplus.service;

import com.biliplus.pojo.vo.AdminMemberVO;
import com.biliplus.result.PageResult;

import java.util.Map;

public interface AdminMemberService {

    PageResult list(String keyword, Integer status, Integer role, Integer page, Integer size);

    AdminMemberVO detail(Long id);

    /** 封禁（同步 user.status=0） */
    void ban(Long userId, String reason, Integer days, Long adminId);

    void unban(Long userId, Long adminId);

    /** 禁言（不影响登录） */
    void mute(Long userId, String reason, Integer days, Long adminId);

    void unmute(Long userId, Long adminId);

    void updateRole(Long userId, Integer role);

    /** 处置记录 */
    Map<String, Object> penalties(Long userId);
}

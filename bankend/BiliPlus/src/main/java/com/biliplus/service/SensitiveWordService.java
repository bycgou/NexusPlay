package com.biliplus.service;

import com.biliplus.pojo.entity.SensitiveWord;
import com.biliplus.pojo.vo.SensitiveHitVO;
import com.biliplus.result.PageResult;

import java.util.List;

public interface SensitiveWordService {

    /**
     * 发布链路统一入口：命中即写命中日志；命中「拦截」级则抛异常中断发布。
     * targetId 允许为空（新内容尚未落库时）。
     */
    void enforce(String text, Long userId, String targetType, Long targetId);

    // ===== 管理端词库维护 =====
    PageResult adminList(String keyword, Integer status, Integer page, Integer size);

    SensitiveWord create(String word, Integer level);

    SensitiveWord update(Long id, Integer level, Integer status);

    void delete(Long id);

    // ===== 命中记录与复核 =====
    PageResult hitList(Integer reviewStatus, String action, Integer page, Integer size);

    /** 复核：1 确认违规 / 2 误伤，用于计算误伤率 */
    void reviewHit(Long adminId, Long hitId, Integer reviewStatus);

    /** 命中统计（误伤率、类型分布、Top 词） */
    java.util.Map<String, Object> hitSummary();
}

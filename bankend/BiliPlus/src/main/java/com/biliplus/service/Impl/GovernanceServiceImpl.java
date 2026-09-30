package com.biliplus.service.Impl;

import com.biliplus.mapper.ReportMapper;
import com.biliplus.mapper.SensitiveHitLogMapper;
import com.biliplus.mapper.UserCreditMapper;
import com.biliplus.mapper.UserPenaltyMapper;
import com.biliplus.service.GovernanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 治理报表聚合。所有指标都从已有表计算，不额外落指标表 */
@Slf4j
@Service
public class GovernanceServiceImpl implements GovernanceService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private SensitiveHitLogMapper sensitiveHitLogMapper;

    @Autowired
    private UserPenaltyMapper userPenaltyMapper;

    @Autowired
    private UserCreditMapper userCreditMapper;

    @Autowired
    private com.biliplus.mapper.EventLogMapper eventLogMapper;

    @Override
    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<>();
        result.put("report", reportMapper.slaSummary());
        result.put("reportDuplicate", reportMapper.duplicateSummary());
        result.put("reasonDistribution", reportMapper.reasonDistribution());
        result.put("sensitiveHit", sensitiveHitLogMapper.reviewSummary());
        result.put("penaltyRecidivism", userPenaltyMapper.recidivismSummary());
        result.put("lowCreditUsers", userCreditMapper.countBelow(UserCreditServiceImpl.THRESHOLD_SHORT));
        // 违规曝光率是治理效果的核心指标，直接放进总览便于看板展示
        result.put("exposure", violationExposure(7));
        return result;
    }

    @Override
    public Map<String, Object> reportSla() {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", reportMapper.slaSummary());
        result.put("handlerWorkload", reportMapper.handlerWorkload());
        return result;
    }

    @Override
    public Map<String, Object> timelinessTrend(int days) {
        int safeDays = days <= 0 ? 7 : Math.min(days, 90);
        LocalDateTime from = LocalDateTime.now().minusDays(safeDays);
        Map<String, Object> result = new HashMap<>();
        result.put("days", safeDays);
        List<Map<String, Object>> rows = reportMapper.dailyTimeliness(from);
        result.put("rows", rows == null ? List.of() : rows);
        return result;
    }

    @Override
    public Map<String, Object> topViolators(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 50);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", userCreditMapper.worstUsers(safeLimit));
        return result;
    }

    @Override
    public Map<String, Object> violationExposure(int days) {
        int safeDays = days <= 0 ? 7 : Math.min(days, 90);
        LocalDateTime from = LocalDateTime.now().minusDays(safeDays);

        Map<String, Object> summary = eventLogMapper.exposureSummary(from);
        long totalViews = toLong(summary == null ? null : summary.get("totalViews"));
        long violationViews = toLong(summary == null ? null : summary.get("violationViews"));

        Map<String, Object> result = new HashMap<>();
        result.put("days", safeDays);
        result.put("totalViews", totalViews);
        result.put("violationViews", violationViews);
        // 分母为 0 时给 null，前端显示 — 而不是误导性的 0%
        result.put("rate", totalViews == 0 ? null : (double) violationViews / totalViews);
        result.put("violationVideoCount", eventLogMapper.countViolationVideos());
        List<Map<String, Object>> rows = eventLogMapper.exposureTrend(from);
        result.put("rows", rows == null ? List.of() : rows);
        return result;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
}

package com.biliplus.service.Impl;

import com.biliplus.mapper.AdminStatsMapper;
import com.biliplus.service.AdminStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台数据看板聚合。口径与各业务表 count 一致，允许近实时。
 * 无数据时返回 0 而不是 null，前端可直接展示。
 */
@Slf4j
@Service
public class AdminStatsServiceImpl implements AdminStatsService {

    @Autowired
    private AdminStatsMapper adminStatsMapper;

    @Override
    public Map<String, Object> overview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Map<String, Object> raw = adminStatsMapper.overview(todayStart);
        Map<String, Object> result = new HashMap<>();
        if (raw != null) {
            raw.forEach((k, v) -> result.put(k, v == null ? 0L : v));
        }
        return result;
    }

    @Override
    public Map<String, Object> trend(int days) {
        int safeDays = days <= 0 ? 7 : Math.min(days, 90);
        LocalDateTime from = LocalDate.now().minusDays(safeDays - 1L).atStartOfDay();

        Map<String, Object> result = new HashMap<>();
        result.put("days", safeDays);
        result.put("videos", nonNull(adminStatsMapper.dailyVideos(from)));
        result.put("users", nonNull(adminStatsMapper.dailyUsers(from)));
        result.put("gifts", nonNull(adminStatsMapper.dailyGifts(from)));
        return result;
    }

    private List<Map<String, Object>> nonNull(List<Map<String, Object>> rows) {
        return rows == null ? List.of() : rows;
    }
}

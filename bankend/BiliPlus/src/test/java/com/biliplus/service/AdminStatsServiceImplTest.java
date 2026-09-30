package com.biliplus.service;

import com.biliplus.mapper.AdminStatsMapper;
import com.biliplus.service.Impl.AdminStatsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceImplTest {

    @Mock
    private AdminStatsMapper adminStatsMapper;

    @InjectMocks
    private AdminStatsServiceImpl adminStatsService;

    @Test
    void overview_shouldReturnNonNullValues() {
        Map<String, Object> raw = new HashMap<>();
        raw.put("totalUsers", 100L);
        raw.put("todayUsers", null); // 缺数时要变成 0，前端不能显示 null
        raw.put("pendingVideos", 3L);
        when(adminStatsMapper.overview(any())).thenReturn(raw);

        Map<String, Object> result = adminStatsService.overview();

        assertEquals(100L, result.get("totalUsers"));
        assertEquals(0L, result.get("todayUsers"));
        assertEquals(3L, result.get("pendingVideos"));
    }

    @Test
    void overview_whenMapperReturnsNull_shouldReturnEmptyMap() {
        when(adminStatsMapper.overview(any())).thenReturn(null);

        Map<String, Object> result = adminStatsService.overview();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void trend_shouldClampDays() {
        when(adminStatsMapper.dailyVideos(any())).thenReturn(List.of());
        when(adminStatsMapper.dailyUsers(any())).thenReturn(List.of());
        when(adminStatsMapper.dailyGifts(any())).thenReturn(List.of());

        Map<String, Object> result = adminStatsService.trend(999);

        assertEquals(90, result.get("days"));
    }

    @Test
    void trend_whenDaysInvalid_shouldDefaultSeven() {
        when(adminStatsMapper.dailyVideos(any())).thenReturn(List.of());
        when(adminStatsMapper.dailyUsers(any())).thenReturn(List.of());
        when(adminStatsMapper.dailyGifts(any())).thenReturn(List.of());

        Map<String, Object> result = adminStatsService.trend(0);

        assertEquals(7, result.get("days"));
    }

    @Test
    void trend_shouldReturnEmptyRowsWhenMapperNull() {
        when(adminStatsMapper.dailyVideos(any())).thenReturn(null);
        when(adminStatsMapper.dailyUsers(any())).thenReturn(null);
        when(adminStatsMapper.dailyGifts(any())).thenReturn(null);

        Map<String, Object> result = adminStatsService.trend(7);

        // 无数据时给空列表而不是 null，避免前端遍历报错
        assertEquals(List.of(), result.get("videos"));
        assertEquals(List.of(), result.get("users"));
        assertEquals(List.of(), result.get("gifts"));
    }
}

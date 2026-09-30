package com.biliplus.service;

import com.biliplus.mapper.ReportMapper;
import com.biliplus.mapper.SensitiveHitLogMapper;
import com.biliplus.mapper.UserCreditMapper;
import com.biliplus.mapper.UserPenaltyMapper;
import com.biliplus.service.Impl.GovernanceServiceImpl;
import com.biliplus.service.Impl.UserCreditServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GovernanceServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private SensitiveHitLogMapper sensitiveHitLogMapper;

    @Mock
    private UserCreditMapper userCreditMapper;

    @Mock
    private UserPenaltyMapper userPenaltyMapper;

    @Mock
    private com.biliplus.mapper.EventLogMapper eventLogMapper;

    @InjectMocks
    private GovernanceServiceImpl governanceService;

    @Test
    void overview_shouldAggregateAllSections() {
        when(reportMapper.slaSummary()).thenReturn(Map.of("total", 10L));
        when(reportMapper.duplicateSummary()).thenReturn(Map.of("dupTargets", 2L));
        when(reportMapper.reasonDistribution()).thenReturn(List.of());
        when(sensitiveHitLogMapper.reviewSummary()).thenReturn(Map.of("total", 3L));
        when(userPenaltyMapper.recidivismSummary()).thenReturn(Map.of("penalizedUsers", 5L));
        when(userCreditMapper.countBelow(UserCreditServiceImpl.THRESHOLD_SHORT)).thenReturn(1L);
        when(eventLogMapper.exposureSummary(any())).thenReturn(Map.of("totalViews", 0L, "violationViews", 0L));
        when(eventLogMapper.countViolationVideos()).thenReturn(0L);
        when(eventLogMapper.exposureTrend(any())).thenReturn(List.of());

        Map<String, Object> result = governanceService.overview();

        // 六块指标都必须有值，论文图表直接吃这个结构
        assertNotNull(result.get("report"));
        assertNotNull(result.get("reportDuplicate"));
        assertNotNull(result.get("reasonDistribution"));
        assertNotNull(result.get("sensitiveHit"));
        assertNotNull(result.get("penaltyRecidivism"));
        assertEquals(1L, result.get("lowCreditUsers"));
        assertNotNull(result.get("exposure"));
    }

    @Test
    void reportSla_shouldIncludeHandlerWorkload() {
        when(reportMapper.slaSummary()).thenReturn(Map.of("pending", 4L));
        when(reportMapper.handlerWorkload()).thenReturn(List.of(Map.of("handlerId", 1L, "cnt", 3L)));

        Map<String, Object> result = governanceService.reportSla();

        assertNotNull(result.get("summary"));
        assertEquals(1, ((List<?>) result.get("handlerWorkload")).size());
    }

    @Test
    void timelinessTrend_shouldClampDays() {
        when(reportMapper.dailyTimeliness(any())).thenReturn(List.of());

        Map<String, Object> result = governanceService.timelinessTrend(999);

        // 上限 90 天，防止一次拉全表
        assertEquals(90, result.get("days"));
    }

    @Test
    void topViolators_shouldClampLimit() {
        when(userCreditMapper.worstUsers(50)).thenReturn(List.of());

        Map<String, Object> result = governanceService.topViolators(999);

        assertNotNull(result.get("rows"));
        verify(userCreditMapper).worstUsers(50);
    }

    @Test
    void violationExposure_shouldComputeRate() {
        when(eventLogMapper.exposureSummary(any()))
                .thenReturn(Map.of("totalViews", 200L, "violationViews", 50L, "distinctVideos", 20L));
        when(eventLogMapper.countViolationVideos()).thenReturn(8L);
        when(eventLogMapper.exposureTrend(any())).thenReturn(List.of(Map.of("day", "2026-03-01")));

        Map<String, Object> result = governanceService.violationExposure(7);

        assertEquals(200L, result.get("totalViews"));
        assertEquals(50L, result.get("violationViews"));
        assertEquals(0.25, (Double) result.get("rate"), 1e-6);
        assertEquals(8L, result.get("violationVideoCount"));
        assertEquals(1, ((List<?>) result.get("rows")).size());
    }

    @Test
    void violationExposure_whenNoViews_shouldReturnNullRate() {
        when(eventLogMapper.exposureSummary(any()))
                .thenReturn(Map.of("totalViews", 0L, "violationViews", 0L, "distinctVideos", 0L));
        when(eventLogMapper.countViolationVideos()).thenReturn(0L);
        when(eventLogMapper.exposureTrend(any())).thenReturn(List.of());

        Map<String, Object> result = governanceService.violationExposure(30);

        // 分母为 0 时给 null，避免前端展示误导性的 0%
        assertNull(result.get("rate"));
        assertEquals(30, result.get("days"));
    }

    @Test
    void violationExposure_shouldClampDays() {
        when(eventLogMapper.exposureSummary(any()))
                .thenReturn(Map.of("totalViews", 1L, "violationViews", 0L, "distinctVideos", 1L));
        when(eventLogMapper.countViolationVideos()).thenReturn(0L);
        when(eventLogMapper.exposureTrend(any())).thenReturn(List.of());

        Map<String, Object> result = governanceService.violationExposure(999);
        assertEquals(90, result.get("days"));
    }
}

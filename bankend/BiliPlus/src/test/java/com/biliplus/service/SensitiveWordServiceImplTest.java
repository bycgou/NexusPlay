package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.SensitiveHitLogMapper;
import com.biliplus.mapper.SensitiveWordMapper;
import com.biliplus.pojo.entity.SensitiveHitLog;
import com.biliplus.pojo.entity.SensitiveWord;
import com.biliplus.service.Impl.SensitiveWordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceImplTest {

    @Mock
    private SensitiveWordMapper sensitiveWordMapper;

    @Mock
    private SensitiveHitLogMapper sensitiveHitLogMapper;

    @Mock
    private UserCreditService userCreditService;

    @Mock
    private AdminOperationLogService adminOperationLogService;

    @InjectMocks
    private SensitiveWordServiceImpl sensitiveWordService;

    private SensitiveWord word(long id, String text, int level) {
        SensitiveWord w = new SensitiveWord();
        w.setId(id);
        w.setWord(text);
        w.setLevel(level);
        w.setStatus(1);
        return w;
    }

    @Test
    void enforce_whenClean_shouldPass() {
        when(sensitiveWordMapper.selectAllEnabled()).thenReturn(List.of(word(1L, "违禁词", 1)));
        sensitiveWordService.init();

        assertDoesNotThrow(() -> sensitiveWordService.enforce("完全正常的内容", 9L, "comment", null));
        verify(sensitiveHitLogMapper, never()).insert(any());
        verify(userCreditService, never()).applyViolation(any(), anyInt(), any());
    }

    @Test
    void enforce_whenBlockingLevel_shouldThrowAndRecord() {
        when(sensitiveWordMapper.selectAllEnabled()).thenReturn(List.of(word(1L, "违禁词", 1)));
        sensitiveWordService.init();

        assertThrows(BusinessException.class,
                () -> sensitiveWordService.enforce("这里有违禁词出现", 9L, "comment", 3L));

        ArgumentCaptor<SensitiveHitLog> captor = ArgumentCaptor.forClass(SensitiveHitLog.class);
        verify(sensitiveHitLogMapper).insert(captor.capture());
        assertEquals("违禁词", captor.getValue().getWord());
        assertEquals("block", captor.getValue().getAction());
        assertEquals(3L, captor.getValue().getTargetId());
        // 拦截级按违规扣分
        verify(userCreditService).applyViolation(eq(9L), anyInt(), anyString());
    }

    @Test
    void enforce_whenMarkOnly_shouldNotThrowButRecord() {
        when(sensitiveWordMapper.selectAllEnabled()).thenReturn(List.of(word(1L, "敏感", 3)));
        sensitiveWordService.init();

        assertDoesNotThrow(() -> sensitiveWordService.enforce("这是敏感内容", 9L, "danmaku", null));

        verify(sensitiveHitLogMapper).insert(any());
        // 仅标记不扣分
        verify(userCreditService, never()).applyViolation(any(), anyInt(), any());
    }

    @Test
    void enforce_whenCaseInsensitive_shouldMatch() {
        when(sensitiveWordMapper.selectAllEnabled()).thenReturn(List.of(word(1L, "BadWord", 1)));
        sensitiveWordService.init();

        assertThrows(BusinessException.class,
                () -> sensitiveWordService.enforce("this is a badword here", 9L, "comment", null));
    }

    @Test
    void enforce_whenEmptyText_shouldPass() {
        assertDoesNotThrow(() -> sensitiveWordService.enforce(null, 9L, "comment", null));
        assertDoesNotThrow(() -> sensitiveWordService.enforce("   ", 9L, "comment", null));
    }

    @Test
    void create_whenDuplicate_shouldThrow() {
        when(sensitiveWordMapper.selectByWord("重复词")).thenReturn(word(1L, "重复词", 1));
        assertThrows(BusinessException.class, () -> sensitiveWordService.create("重复词", 1));
        verify(sensitiveWordMapper, never()).insert(any());
    }

    @Test
    void create_whenBlank_shouldThrow() {
        assertThrows(BusinessException.class, () -> sensitiveWordService.create("  ", 1));
    }

    @Test
    void reviewHit_whenStatusIllegal_shouldThrow() {
        assertThrows(BusinessException.class, () -> sensitiveWordService.reviewHit(1L, 5L, 0));
        verify(sensitiveHitLogMapper, never()).review(any(), any(), any(), any());
    }

    @Test
    void reviewHit_whenAlreadyReviewed_shouldThrow() {
        when(sensitiveHitLogMapper.review(eq(5L), eq(2), eq(1L), any())).thenReturn(0);
        assertThrows(BusinessException.class, () -> sensitiveWordService.reviewHit(1L, 5L, 2));
    }

    @Test
    void hitSummary_shouldComputeFalsePositiveRate() {
        when(sensitiveHitLogMapper.reviewSummary())
                .thenReturn(Map.of("total", 10L, "confirmed", 6L, "falsePositive", 4L));
        when(sensitiveHitLogMapper.countByTargetType()).thenReturn(List.of());
        when(sensitiveHitLogMapper.topWords(10)).thenReturn(List.of());

        Map<String, Object> summary = sensitiveWordService.hitSummary();

        assertEquals(10L, summary.get("total"));
        assertEquals(10L, summary.get("reviewed"));
        assertEquals(0.4, (Double) summary.get("falsePositiveRate"), 1e-6);
    }

    @Test
    void hitSummary_whenNoReview_shouldReturnNullRate() {
        when(sensitiveHitLogMapper.reviewSummary())
                .thenReturn(Map.of("total", 5L, "confirmed", 0L, "falsePositive", 0L));
        when(sensitiveHitLogMapper.countByTargetType()).thenReturn(List.of());
        when(sensitiveHitLogMapper.topWords(10)).thenReturn(List.of());

        Map<String, Object> summary = sensitiveWordService.hitSummary();

        assertNull(summary.get("falsePositiveRate"));
    }
}

package com.biliplus.service;

import com.biliplus.constant.EventType;
import com.biliplus.mapper.EventLogMapper;
import com.biliplus.pojo.entity.EventLog;
import com.biliplus.service.Impl.EventLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventLogServiceImplTest {

    @Mock
    private EventLogMapper eventLogMapper;

    @InjectMocks
    private EventLogServiceImpl eventLogService;

    @Test
    void record_shouldFillDefaults() {
        eventLogService.record(EventType.VIDEO_VIEW, 9L, EventType.TARGET_VIDEO, 3L);

        ArgumentCaptor<EventLog> captor = ArgumentCaptor.forClass(EventLog.class);
        verify(eventLogMapper).insert(captor.capture());
        EventLog saved = captor.getValue();
        assertEquals(EventType.VIDEO_VIEW, saved.getEventType());
        assertEquals(9L, saved.getUserId());
        assertEquals("video", saved.getTargetType());
        assertEquals(3L, saved.getTargetId());
        // 未指定来源时默认 web
        assertEquals("web", saved.getSource());
        assertNotNull(saved.getCreateTime());
    }

    @Test
    void record_shouldPassDurationAndExtra() {
        eventLogService.record(EventType.LIVE_WATCH, 9L, EventType.TARGET_LIVE_ROOM, 1L,
                120, "{\"x\":1}", EventType.SOURCE_ADMIN);

        ArgumentCaptor<EventLog> captor = ArgumentCaptor.forClass(EventLog.class);
        verify(eventLogMapper).insert(captor.capture());
        assertEquals(120, captor.getValue().getDurationSec());
        assertEquals("{\"x\":1}", captor.getValue().getExtra());
        assertEquals("admin", captor.getValue().getSource());
    }

    @Test
    void record_whenExtraTooLong_shouldTruncate() {
        String longText = "x".repeat(600);

        eventLogService.record(EventType.COMMENT, 9L, EventType.TARGET_VIDEO, 3L,
                null, longText, EventType.SOURCE_WEB);

        ArgumentCaptor<EventLog> captor = ArgumentCaptor.forClass(EventLog.class);
        verify(eventLogMapper).insert(captor.capture());
        assertEquals(500, captor.getValue().getExtra().length());
    }

    @Test
    void record_whenMapperFails_shouldNotPropagate() {
        doThrow(new RuntimeException("db down")).when(eventLogMapper).insert(any());

        // 埋点是旁路逻辑，绝不能影响主业务
        assertDoesNotThrow(() -> eventLogService.record(EventType.VIDEO_VIEW, 9L, "video", 3L));
    }

    @Test
    void record_whenEventTypeBlank_shouldSkip() {
        eventLogService.record(null, 9L, "video", 3L);
        eventLogService.record("  ", 9L, "video", 3L);
        verify(eventLogMapper, never()).insert(any());
    }

    @Test
    void record_whenAnonymous_shouldAllowNullUserId() {
        eventLogService.record(EventType.VIDEO_VIEW, null, EventType.TARGET_VIDEO, 3L);

        ArgumentCaptor<EventLog> captor = ArgumentCaptor.forClass(EventLog.class);
        verify(eventLogMapper).insert(captor.capture());
        assertNull(captor.getValue().getUserId());
    }
}

package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.LiveMicSessionMapper;
import com.biliplus.mapper.LiveRoomMapper;
import com.biliplus.pojo.entity.LiveMicSession;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.service.Impl.LiveMicServiceImpl;
import com.biliplus.websocket.LiveWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveMicServiceImplTest {

    @Mock
    private LiveMicSessionMapper micSessionMapper;

    @Mock
    private LiveRoomMapper liveRoomMapper;

    @Mock
    private LiveWebSocketHandler liveWebSocketHandler;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LiveMicServiceImpl liveMicService;

    private LiveRoom room() {
        LiveRoom room = new LiveRoom();
        room.setId(1L);
        room.setUserId(100L);
        room.setStatus(1);
        return room;
    }

    @Test
    void apply_whenRoomOffline_shouldThrow() {
        LiveRoom room = room();
        room.setStatus(2);
        when(liveRoomMapper.selectById(1L)).thenReturn(room);
        assertThrows(BusinessException.class, () -> liveMicService.apply(9L, 1L));
    }

    @Test
    void apply_whenHostApply_shouldThrow() {
        when(liveRoomMapper.selectById(1L)).thenReturn(room());
        assertThrows(BusinessException.class, () -> liveMicService.apply(100L, 1L));
    }

    @Test
    void apply_shouldInsertPending() {
        when(liveRoomMapper.selectById(1L)).thenReturn(room());
        when(micSessionMapper.selectActive(1L)).thenReturn(null);
        when(micSessionMapper.listPending(1L, 5)).thenReturn(List.of());

        LiveMicSession session = liveMicService.apply(9L, 1L);
        assertEquals(0, session.getStatus());
        assertEquals(9L, session.getGuestUserId());
        verify(micSessionMapper).insert(any(LiveMicSession.class));
    }

    @Test
    void accept_whenNotHost_shouldThrow() {
        LiveMicSession s = new LiveMicSession();
        s.setId(3L);
        s.setHostUserId(100L);
        s.setGuestUserId(9L);
        s.setLiveRoomId(1L);
        s.setStatus(0);
        when(micSessionMapper.selectById(3L)).thenReturn(s);
        assertThrows(BusinessException.class, () -> liveMicService.accept(9L, 3L, true));
    }

    @Test
    void accept_agree_shouldSetRunning() {
        LiveMicSession s = new LiveMicSession();
        s.setId(3L);
        s.setHostUserId(100L);
        s.setGuestUserId(9L);
        s.setLiveRoomId(1L);
        s.setStatus(0);
        when(micSessionMapper.selectById(3L)).thenReturn(s);

        Map<String, Object> result = liveMicService.accept(100L, 3L, true);
        assertEquals(1, s.getStatus());
        assertNotNull(result.get("rtcRoom"));
        verify(micSessionMapper).updateStatus(s);
    }

    @Test
    void accept_refuse_shouldSetRejected() {
        LiveMicSession s = new LiveMicSession();
        s.setId(3L);
        s.setHostUserId(100L);
        s.setGuestUserId(9L);
        s.setLiveRoomId(1L);
        s.setStatus(0);
        when(micSessionMapper.selectById(3L)).thenReturn(s);

        liveMicService.accept(100L, 3L, false);
        assertEquals(3, s.getStatus());
    }

    @Test
    void leave_shouldCloseActive() {
        LiveMicSession s = new LiveMicSession();
        s.setId(3L);
        s.setHostUserId(100L);
        s.setGuestUserId(9L);
        s.setLiveRoomId(1L);
        s.setStatus(1);
        when(micSessionMapper.selectActive(1L)).thenReturn(s);

        liveMicService.leave(9L, 1L);
        assertEquals(2, s.getStatus());
        verify(micSessionMapper).updateStatus(s);
    }
}

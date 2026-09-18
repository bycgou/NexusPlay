package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.LivePkMapper;
import com.biliplus.mapper.LiveRoomMapper;
import com.biliplus.pojo.entity.LivePk;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.properties.LiveProperties;
import com.biliplus.service.Impl.LivePkServiceImpl;
import com.biliplus.websocket.LiveWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivePkServiceImplTest {

    @Mock
    private LivePkMapper livePkMapper;

    @Mock
    private LiveRoomMapper liveRoomMapper;

    @Mock
    private LiveWebSocketHandler liveWebSocketHandler;

    @Spy
    private LiveProperties liveProperties = new LiveProperties();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LivePkServiceImpl livePkService;

    private LiveRoom room(long id, long hostId, int status) {
        LiveRoom r = new LiveRoom();
        r.setId(id);
        r.setUserId(hostId);
        r.setStatus(status);
        r.setTitle("room-" + id);
        r.setPlayUrl("http://x/" + id);
        return r;
    }

    @Test
    void invite_whenNotLive_shouldThrow() {
        when(liveRoomMapper.selectLiveByUserId(9L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> livePkService.invite(9L, 2L));
        verify(livePkMapper, never()).insert(any());
    }

    @Test
    void invite_whenOpponentOffline_shouldThrow() {
        when(liveRoomMapper.selectLiveByUserId(9L)).thenReturn(room(1L, 9L, 1));
        when(liveRoomMapper.selectById(2L)).thenReturn(room(2L, 8L, 2));
        assertThrows(BusinessException.class, () -> livePkService.invite(9L, 2L));
    }

    @Test
    void invite_shouldCreatePending() {
        when(liveRoomMapper.selectLiveByUserId(9L)).thenReturn(room(1L, 9L, 1));
        when(liveRoomMapper.selectById(2L)).thenReturn(room(2L, 8L, 1));
        when(livePkMapper.selectActiveByRoom(anyLong())).thenReturn(null);

        LivePk pk = livePkService.invite(9L, 2L);
        assertEquals(0, pk.getStatus());
        assertEquals(1L, pk.getRoomAId());
        assertEquals(2L, pk.getRoomBId());
        verify(livePkMapper).insert(any(LivePk.class));
    }

    @Test
    void response_whenNotInvitee_shouldThrow() {
        LivePk pk = new LivePk();
        pk.setId(5L);
        pk.setHostBId(8L);
        pk.setStatus(0);
        when(livePkMapper.selectById(5L)).thenReturn(pk);
        assertThrows(BusinessException.class, () -> livePkService.response(9L, 5L, true));
    }

    @Test
    void response_agree_shouldStartAndBroadcast() {
        LivePk pk = new LivePk();
        pk.setId(5L);
        pk.setHostBId(8L);
        pk.setHostAId(9L);
        pk.setRoomAId(1L);
        pk.setRoomBId(2L);
        pk.setStatus(0);
        pk.setScoreA(0);
        pk.setScoreB(0);
        pk.setDurationSec(300);
        when(livePkMapper.selectById(5L)).thenReturn(pk);
        when(liveRoomMapper.selectById(1L)).thenReturn(room(1L, 9L, 1));
        when(liveRoomMapper.selectById(2L)).thenReturn(room(2L, 8L, 1));

        LivePk started = livePkService.response(8L, 5L, true);
        assertEquals(1, started.getStatus());
        verify(liveWebSocketHandler).broadcast(eq(1L), anyString());
        verify(liveWebSocketHandler).broadcast(eq(2L), anyString());
    }

    @Test
    void addScore_shouldUpdateSideA() {
        LivePk pk = new LivePk();
        pk.setId(5L);
        pk.setRoomAId(1L);
        pk.setRoomBId(2L);
        pk.setStatus(1);
        pk.setScoreA(0);
        pk.setScoreB(0);
        when(livePkMapper.selectById(5L)).thenReturn(pk).thenReturn(pk);
        when(livePkMapper.addScoreA(5L, 1L, 100)).thenReturn(1);

        LivePk updated = livePkService.addScore(5L, 1L, 100);
        verify(livePkMapper).addScoreA(5L, 1L, 100);
        assertNotNull(updated);
    }

    @Test
    void end_shouldMarkFinished() {
        LivePk pk = new LivePk();
        pk.setId(5L);
        pk.setHostAId(9L);
        pk.setHostBId(8L);
        pk.setRoomAId(1L);
        pk.setRoomBId(2L);
        pk.setStatus(1);
        pk.setScoreA(200);
        pk.setScoreB(80);
        when(livePkMapper.selectById(5L)).thenReturn(pk);

        LivePk ended = livePkService.end(9L, 5L);
        assertEquals(2, ended.getStatus());
        verify(livePkMapper).update(any(LivePk.class));
    }
}

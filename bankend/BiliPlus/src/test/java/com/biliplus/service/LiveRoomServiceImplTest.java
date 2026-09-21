package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.LiveRoomMapper;
import com.biliplus.mapper.LiveUserMapper;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.pojo.dto.LiveStartDTO;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.pojo.vo.LiveRoomVO;
import com.biliplus.properties.LiveProperties;
import com.biliplus.result.PageResult;
import com.biliplus.service.Impl.LiveRoomServiceImpl;
import com.biliplus.websocket.LiveWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveRoomServiceImplTest {

    @Mock
    private LiveRoomMapper liveRoomMapper;

    @Mock
    private LiveUserMapper liveUserMapper;

    @Mock
    private PeopleUserMapper peopleUserMapper;

    @Mock
    private LiveWebSocketHandler liveWebSocketHandler;

    @Mock
    private LiveMicService liveMicService;

    @Mock
    private LivePkService livePkService;

    @Spy
    private LiveProperties liveProperties = new LiveProperties();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LiveRoomServiceImpl liveRoomService;

    private LiveStartDTO validDto() {
        LiveStartDTO dto = new LiveStartDTO();
        dto.setTitle("测试直播");
        dto.setCategoryId(1);
        dto.setCoverUrl("/cover.jpg");
        return dto;
    }

    private LiveRoom liveRoom() {
        LiveRoom room = new LiveRoom();
        room.setId(1L);
        room.setUserId(9L);
        room.setStatus(1);
        room.setTitle("测试直播");
        room.setStreamKey("key123");
        room.setPlayUrl("http://localhost:8080/live/key123.live.flv");
        room.setPushUrl("rtmp://localhost:1935/live/key123");
        return room;
    }

    @BeforeEach
    void setUp() {
        // LiveProperties defaults already set
    }

    @Test
    void startLive_whenTitleBlank_shouldThrow() {
        LiveStartDTO dto = validDto();
        dto.setTitle(" ");
        assertThrows(BusinessException.class, () -> liveRoomService.startLive(9L, dto));
        verify(liveRoomMapper, never()).insert(any());
    }

    @Test
    void startLive_whenCategoryNull_shouldThrow() {
        LiveStartDTO dto = validDto();
        dto.setCategoryId(null);
        assertThrows(BusinessException.class, () -> liveRoomService.startLive(9L, dto));
    }

    @Test
    void startLive_whenAlreadyLive_shouldThrow() {
        when(liveRoomMapper.selectLiveByUserId(9L)).thenReturn(liveRoom());
        assertThrows(BusinessException.class, () -> liveRoomService.startLive(9L, validDto()));
        verify(liveRoomMapper, never()).insert(any());
    }

    @Test
    void startLive_shouldCreateRoomWithStreamKey() {
        when(liveRoomMapper.selectLiveByUserId(9L)).thenReturn(null);
        LiveRoomVO vo = liveRoomService.startLive(9L, validDto());
        assertNotNull(vo);
        verify(liveRoomMapper).insert(any(LiveRoom.class));
    }

    @Test
    void stopLive_whenNotHost_shouldThrow() {
        when(liveRoomMapper.selectById(1L)).thenReturn(liveRoom());
        assertThrows(BusinessException.class, () -> liveRoomService.stopLive(1L, 88L));
        verify(liveRoomMapper, never()).updateStatus(any());
    }

    @Test
    void stopLive_shouldMarkEndedAndCloseMicPk() {
        when(liveRoomMapper.selectById(1L)).thenReturn(liveRoom());
        liveRoomService.stopLive(1L, 9L);
        verify(liveRoomMapper).updateStatus(any());
        verify(liveMicService).forceCloseByRoom(1L);
        verify(livePkService).forceCloseByRoom(1L);
    }

    @Test
    void listLive_shouldPageAndFilter() {
        when(liveRoomMapper.listLive(0, 12)).thenReturn(List.of(liveRoom()));
        when(liveRoomMapper.countLive()).thenReturn(1L);
        when(liveWebSocketHandler.onlineCount(1L)).thenReturn(3);

        PageResult page = liveRoomService.listLive(1, 12);
        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
    }
}

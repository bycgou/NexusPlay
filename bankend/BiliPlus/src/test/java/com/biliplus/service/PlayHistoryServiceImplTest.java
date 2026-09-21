package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.PlayHistoryMapper;
import com.biliplus.pojo.entity.PlayHistory;
import com.biliplus.result.PageResult;
import com.biliplus.service.Impl.PlayHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayHistoryServiceImplTest {

    @Mock
    private PlayHistoryMapper playHistoryMapper;

    @InjectMocks
    private PlayHistoryServiceImpl playHistoryService;

    @Test
    void record_whenNotLogin_shouldSkipSilently() {
        playHistoryService.record(null, 1L, 10, 100);
        verify(playHistoryMapper, never()).upsert(any());
    }

    @Test
    void record_whenVideoIdMissing_shouldThrow() {
        assertThrows(BusinessException.class, () -> playHistoryService.record(9L, null, 10, 100));
    }

    @Test
    void record_shouldUpsertWithNonNegativeValues() {
        playHistoryService.record(9L, 3L, -5, null);

        ArgumentCaptor<PlayHistory> captor = ArgumentCaptor.forClass(PlayHistory.class);
        verify(playHistoryMapper).upsert(captor.capture());
        PlayHistory saved = captor.getValue();
        assertEquals(9L, saved.getUserId());
        assertEquals(3L, saved.getVideoId());
        assertEquals(0, saved.getProgressSec());
        assertEquals(0, saved.getDurationSec());
    }

    @Test
    void getProgress_whenNotLogin_shouldReturnNull() {
        assertNull(playHistoryService.getProgress(null, 3L));
    }

    @Test
    void list_whenNotLogin_shouldThrow() {
        assertThrows(BusinessException.class, () -> playHistoryService.list(null, 1, 20));
    }

    @Test
    void list_shouldPage() {
        when(playHistoryMapper.countByUser(9L)).thenReturn(3L);
        when(playHistoryMapper.pageByUser(9L, 0, 20)).thenReturn(List.of());

        PageResult result = playHistoryService.list(9L, 1, 20);

        assertEquals(3L, result.getTotal());
    }

    @Test
    void clear_shouldDeleteAllOfCurrentUser() {
        playHistoryService.clear(9L);
        verify(playHistoryMapper).deleteAll(9L);
    }

    @Test
    void clear_whenNotLogin_shouldThrow() {
        assertThrows(BusinessException.class, () -> playHistoryService.clear(null));
        verify(playHistoryMapper, never()).deleteAll(any());
    }
}

package com.biliplus.service;

import com.biliplus.mapper.VideoFeatureMapper;
import com.biliplus.service.Impl.VideoFeatureServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoFeatureServiceImplTest {

    @Mock
    private VideoFeatureMapper videoFeatureMapper;

    @InjectMocks
    private VideoFeatureServiceImpl videoFeatureService;

    @Test
    void enterPool_shouldPersistFeatures() {
        videoFeatureService.enterPool(3L, 120, 0);
        verify(videoFeatureMapper).enterPool(3L, 120, 0);
    }

    @Test
    void enterPool_whenIsOriginalNull_shouldDefaultOriginal() {
        videoFeatureService.enterPool(3L, null, null);
        verify(videoFeatureMapper).enterPool(eq(3L), eq(null), eq(1));
    }

    @Test
    void enterPool_whenMapperFails_shouldNotThrow() {
        doThrow(new RuntimeException("db")).when(videoFeatureMapper).enterPool(any(), any(), any());
        videoFeatureService.enterPool(3L, 10, 1);
    }

    @Test
    void leavePool_shouldCallMapper() {
        videoFeatureService.leavePool(9L);
        verify(videoFeatureMapper).leavePool(9L);
    }

    @Test
    void ensurePool_shouldSyncPoolAndMarkOffline() {
        when(videoFeatureMapper.ensurePool()).thenReturn(3);
        when(videoFeatureMapper.markOffline()).thenReturn(1);
        assertTrue(videoFeatureService.ensurePool());
        verify(videoFeatureMapper).ensurePool();
        verify(videoFeatureMapper).markOffline();
    }

    @Test
    void refreshHotScores_shouldReturnUpdatedCount() {
        when(videoFeatureMapper.refreshHotScores()).thenReturn(12);
        assertEquals(12, videoFeatureService.refreshHotScores());
    }

    @Test
    void scheduledRefresh_shouldEnsureThenRefresh() {
        when(videoFeatureMapper.ensurePool()).thenReturn(0);
        when(videoFeatureMapper.markOffline()).thenReturn(0);
        when(videoFeatureMapper.refreshHotScores()).thenReturn(5);
        videoFeatureService.scheduledRefresh();
        verify(videoFeatureMapper).ensurePool();
        verify(videoFeatureMapper).refreshHotScores();
    }

    @Test
    void refreshHotScores_whenMapperFails_shouldReturnZero() {
        when(videoFeatureMapper.refreshHotScores()).thenThrow(new RuntimeException("db"));
        assertEquals(0, videoFeatureService.refreshHotScores());
    }
}

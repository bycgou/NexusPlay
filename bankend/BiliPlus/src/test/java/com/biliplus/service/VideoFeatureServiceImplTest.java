package com.biliplus.service;

import com.biliplus.mapper.VideoFeatureMapper;
import com.biliplus.service.Impl.VideoFeatureServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}

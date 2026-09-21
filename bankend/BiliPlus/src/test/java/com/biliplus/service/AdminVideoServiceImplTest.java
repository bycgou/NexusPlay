package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.AdminVideoMapper;
import com.biliplus.pojo.entity.Video;
import com.biliplus.result.PageResult;
import com.biliplus.service.Impl.AdminVideoServiceImpl;
import com.github.pagehelper.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminVideoServiceImplTest {

    @Mock
    private AdminVideoMapper adminVideoMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DynamicService dynamicService;

    @InjectMocks
    private AdminVideoServiceImpl adminVideoService;

    @Test
    void approve_whenUpdateFails_shouldThrow() {
        when(adminVideoMapper.updateStatus(1L, 0, 1)).thenReturn(0);
        assertThrows(BusinessException.class, () -> adminVideoService.approve(1L));
    }

    @Test
    void approve_shouldUpdatePendingToNormal() {
        when(adminVideoMapper.updateStatus(2L, 0, 1)).thenReturn(1);
        adminVideoService.approve(2L);
        verify(adminVideoMapper).updateStatus(2L, 0, 1);
        verify(adminVideoMapper).clearRejectReason(2L);
    }

    @Test
    void offline_shouldUpdateNormalToOffline() {
        when(adminVideoMapper.updateStatus(3L, 1, 2)).thenReturn(1);
        adminVideoService.offline(3L);
        verify(adminVideoMapper).updateStatus(3L, 1, 2);
    }

    @Test
    void offline_whenIdNull_shouldThrow() {
        assertThrows(BusinessException.class, () -> adminVideoService.offline(null));
        verify(adminVideoMapper, never()).updateStatus(anyLong(), anyInt(), anyInt());
    }

    @Test
    void pageByStatus_shouldReturnResult() {
        Page<Video> page = new Page<>(1, 20);
        page.setTotal(1);
        Video v = new Video();
        v.setId(9L);
        page.add(v);
        when(adminVideoMapper.selectByStatus(0)).thenReturn(page);

        PageResult result = adminVideoService.pageByStatus(0, 1, 20);
        assertEquals(1L, result.getTotal());
    }

    @Test
    void getById_shouldReturnVideo() {
        Video v = new Video();
        v.setId(11L);
        when(adminVideoMapper.selectById(11L)).thenReturn(v);
        assertEquals(11L, adminVideoService.getById(11L).getId());
    }

    @Test
    void getById_whenNull_shouldReturnNull() {
        assertNull(adminVideoService.getById(null));
    }

    @Test
    void reject_whenReasonBlank_shouldThrow() {
        assertThrows(BusinessException.class, () -> adminVideoService.reject(1L, "  "));
        verify(adminVideoMapper, never()).reject(anyLong(), anyInt(), anyInt(), anyString());
    }

    @Test
    void reject_shouldCallMapperWithReason() {
        when(adminVideoMapper.reject(5L, 0, 3, "内容违规")).thenReturn(1);
        adminVideoService.reject(5L, " 内容违规 ");
        verify(adminVideoMapper).reject(5L, 0, 3, "内容违规");
    }

    @Test
    void reject_whenUpdateFails_shouldThrow() {
        when(adminVideoMapper.reject(6L, 0, 3, "原因")).thenReturn(0);
        assertThrows(BusinessException.class, () -> adminVideoService.reject(6L, "原因"));
    }
}

package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.BannerMapper;
import com.biliplus.pojo.entity.Banner;
import com.biliplus.service.Impl.BannerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @Mock
    private BannerMapper bannerMapper;

    @InjectMocks
    private BannerServiceImpl bannerService;

    private Banner validBanner() {
        Banner b = new Banner();
        b.setTitle("测试");
        b.setImageUrl("/x.jpg");
        b.setLinkType(3);
        return b;
    }

    @Test
    void listOnline_shouldReturnMapper() {
        when(bannerMapper.listOnline()).thenReturn(List.of(validBanner()));
        assertEquals(1, bannerService.listOnline().size());
    }

    @Test
    void create_whenTitleBlank_shouldThrow() {
        Banner b = new Banner();
        b.setImageUrl("/x.jpg");
        assertThrows(BusinessException.class, () -> bannerService.create(b));
        verify(bannerMapper, never()).insert(any());
    }

    @Test
    void create_whenLinkTypeVideoWithoutId_shouldThrow() {
        Banner b = validBanner();
        b.setLinkType(1);
        assertThrows(BusinessException.class, () -> bannerService.create(b));
    }

    @Test
    void create_shouldFillDefaultsAndInsert() {
        Banner b = validBanner();
        bannerService.create(b);
        assertEquals(1, b.getStatus());
        assertEquals(0, b.getSortOrder());
        verify(bannerMapper).insert(b);
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        when(bannerMapper.selectById(9L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> bannerService.update(9L, validBanner()));
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(bannerMapper.selectById(1L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> bannerService.delete(1L));
    }

    @Test
    void delete_shouldCallMapper() {
        Banner b = validBanner();
        b.setId(2L);
        when(bannerMapper.selectById(2L)).thenReturn(b);
        bannerService.delete(2L);
        verify(bannerMapper).deleteById(2L);
    }
}

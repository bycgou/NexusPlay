package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.DynamicMapper;
import com.biliplus.mapper.VideoMapper;
import com.biliplus.pojo.entity.Dynamic;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.DynamicVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.Impl.DynamicServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicServiceImplTest {

    @Mock
    private DynamicMapper dynamicMapper;

    @Mock
    private VideoMapper videoMapper;

    @InjectMocks
    private DynamicServiceImpl dynamicService;

    @Test
    void publishText_whenNotLogin_shouldThrow() {
        assertThrows(BusinessException.class, () -> dynamicService.publishText(null, "hi"));
    }

    @Test
    void publishText_whenBlank_shouldThrow() {
        assertThrows(BusinessException.class, () -> dynamicService.publishText(9L, "   "));
        verify(dynamicMapper, never()).insert(any());
    }

    @Test
    void publishText_whenTooLong_shouldThrow() {
        String long1001 = "x".repeat(1001);
        assertThrows(BusinessException.class, () -> dynamicService.publishText(9L, long1001));
        verify(dynamicMapper, never()).insert(any());
    }

    @Test
    void publishText_shouldStoreTrimmedContent() {
        dynamicService.publishText(9L, "  今天天气不错  ");

        ArgumentCaptor<Dynamic> captor = ArgumentCaptor.forClass(Dynamic.class);
        verify(dynamicMapper).insert(captor.capture());
        assertEquals("今天天气不错", captor.getValue().getContent());
        assertEquals(1, captor.getValue().getType());
    }

    @Test
    void publishVideoDynamic_whenAlreadyPublished_shouldSkip() {
        when(dynamicMapper.countVideoDynamic(3L)).thenReturn(1L);

        dynamicService.publishVideoDynamic(3L);

        verify(dynamicMapper, never()).insert(any());
    }

    @Test
    void publishVideoDynamic_shouldCopyVideoOwnerAndTitle() {
        when(dynamicMapper.countVideoDynamic(3L)).thenReturn(0L);
        Video video = new Video();
        video.setId(3L);
        video.setUserId(100L);
        video.setTitle("我的第一支视频");
        when(videoMapper.getVideo(3L)).thenReturn(video);

        dynamicService.publishVideoDynamic(3L);

        ArgumentCaptor<Dynamic> captor = ArgumentCaptor.forClass(Dynamic.class);
        verify(dynamicMapper).insert(captor.capture());
        Dynamic saved = captor.getValue();
        assertEquals(100L, saved.getUserId());
        assertEquals(2, saved.getType());
        assertEquals(3L, saved.getVideoId());
        assertEquals("我的第一支视频", saved.getContent());
    }

    @Test
    void publishVideoDynamic_whenVideoMissing_shouldSkip() {
        when(dynamicMapper.countVideoDynamic(3L)).thenReturn(0L);
        when(videoMapper.getVideo(3L)).thenReturn(null);

        dynamicService.publishVideoDynamic(3L);

        verify(dynamicMapper, never()).insert(any());
    }

    @Test
    void feed_whenNotLogin_shouldThrow() {
        assertThrows(BusinessException.class, () -> dynamicService.feed(null, 1, 20));
    }

    @Test
    void feed_shouldQueryByCurrentUserAndMarkLiked() {
        DynamicVO mine = new DynamicVO();
        mine.setId(11L);
        when(dynamicMapper.selectFeed(9L, 0, 20)).thenReturn(List.of(mine));
        when(dynamicMapper.countFeed(9L)).thenReturn(1L);
        when(dynamicMapper.selectLikedIds(9L, List.of(11L))).thenReturn(List.of(11L));

        PageResult result = dynamicService.feed(9L, 1, 20);

        assertEquals(1L, result.getTotal());
        assertEquals(true, mine.getLiked());
    }

    @Test
    void hot_shouldWorkForAnonymousViewer() {
        DynamicVO item = new DynamicVO();
        item.setId(11L);
        when(dynamicMapper.selectHot(0, 20)).thenReturn(List.of(item));
        when(dynamicMapper.countHot()).thenReturn(1L);

        PageResult result = dynamicService.hot(null, 1, 20);

        assertEquals(1L, result.getTotal());
        // 未登录时无点赞态，但必须给出明确的 false 而不是 null
        assertEquals(false, item.getLiked());
    }

    @Test
    void toggleLike_shouldInsertWhenNotLiked() {
        Dynamic dynamic = new Dynamic();
        dynamic.setId(11L);
        dynamic.setStatus(1);
        when(dynamicMapper.selectById(11L)).thenReturn(dynamic);
        when(dynamicMapper.countLike(9L, 11L)).thenReturn(0);
        when(dynamicMapper.countLikes(11L)).thenReturn(5L);

        Map<String, Object> result = dynamicService.toggleLike(9L, 11L);

        assertEquals(true, result.get("liked"));
        assertEquals(5L, result.get("likeCount"));
        verify(dynamicMapper).insertLike(eq(9L), eq(11L), any());
    }

    @Test
    void delete_whenNotOwner_shouldThrow() {
        Dynamic dynamic = new Dynamic();
        dynamic.setId(11L);
        dynamic.setUserId(100L);
        dynamic.setStatus(1);
        when(dynamicMapper.selectById(11L)).thenReturn(dynamic);

        assertThrows(BusinessException.class, () -> dynamicService.delete(9L, 11L));
        verify(dynamicMapper, never()).softDelete(anyLong(), anyLong());
    }
}

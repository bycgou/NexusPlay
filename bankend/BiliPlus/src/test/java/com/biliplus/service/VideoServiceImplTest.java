package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.pojo.dto.userdto.VideoPageQueryDTO;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.GetListVideoVO;
import com.biliplus.pojo.vo.HotVideoVO;
import com.biliplus.result.PageResult;
import com.biliplus.mapper.VideoMapper;
import com.github.pagehelper.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceImplTest {

    @Mock
    private VideoMapper videoMapper;

    @Mock
    private PeopleUserMapper peopleUserMapper;

    @InjectMocks
    private VideoServiceImpl videoService;

    @Test
    void getVideo_whenNull_shouldThrow() {
        when(videoMapper.getVideo(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> videoService.getVideo(999L));
    }

    @Test
    void recommend_shouldReturnPageResult() {
        Page<GetListVideoVO> page = new Page<>(1, 10);
        page.setTotal(0);
        when(videoMapper.recommend(0, 20)).thenReturn(page);
        PageResult result = videoService.recommend(1, 20);
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
    }

    @Test
    void hot_shouldAssignRankFromOne() {
        HotVideoVO a = new HotVideoVO();
        a.setTitle("a");
        HotVideoVO b = new HotVideoVO();
        b.setTitle("b");
        when(videoMapper.hotRank(10)).thenReturn(java.util.Arrays.asList(a, b));

        java.util.List<HotVideoVO> list = videoService.hot(10);

        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getRank());
        assertEquals(2, list.get(1).getRank());
    }

    @Test
    void hot_whenSizeNull_shouldDefault20() {
        when(videoMapper.hotRank(20)).thenReturn(java.util.Collections.emptyList());
        java.util.List<HotVideoVO> list = videoService.hot(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(videoMapper).hotRank(20);
    }

    @Test
    void hot_whenSizeTooLarge_shouldCapAt50() {
        when(videoMapper.hotRank(50)).thenReturn(java.util.Collections.emptyList());
        videoService.hot(999);
        verify(videoMapper).hotRank(50);
    }
}

package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.pojo.dto.userdto.VideoPageQueryDTO;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.GetListVideoVO;
import com.biliplus.result.PageResult;
import com.biliplus.mapper.VideoMapper;
import com.github.pagehelper.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
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
        when(videoMapper.recommend()).thenReturn(page);
        PageResult result = videoService.recommend();
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
    }
}

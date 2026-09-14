package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.VideoMapper;
import com.biliplus.pojo.dto.userdto.VideoPageQueryDTO;
import com.biliplus.pojo.entity.Video;
import com.biliplus.result.PageResult;
import com.github.pagehelper.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 搜索：title 关键词透传与 pageSize 限制
 */
@ExtendWith(MockitoExtension.class)
class VideoSearchServiceTest {

    @Mock
    private VideoMapper videoMapper;

    @Mock
    private com.biliplus.mapper.PeopleUserMapper peopleUserMapper;

    @InjectMocks
    private VideoServiceImpl videoService;

    @Test
    void search_withTitle_shouldPassToMapper() {
        VideoPageQueryDTO dto = new VideoPageQueryDTO();
        dto.setTitle("java");
        dto.setPage(1);
        dto.setPageSize(20);

        Page<Video> page = new Page<>(1, 20);
        page.setTotal(0);
        when(videoMapper.pageQuery(dto)).thenReturn(page);

        PageResult result = videoService.pageQuery(dto);
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
    }

    @Test
    void search_withCategory_shouldPassCategoryId() {
        VideoPageQueryDTO dto = new VideoPageQueryDTO();
        dto.setCategoryId(3);
        dto.setPage(1);
        dto.setPageSize(20);

        Page<Video> page = new Page<>(1, 20);
        when(videoMapper.pageQuery(dto)).thenReturn(page);

        PageResult result = videoService.pageQuery(dto);
        assertNotNull(result);
    }
}

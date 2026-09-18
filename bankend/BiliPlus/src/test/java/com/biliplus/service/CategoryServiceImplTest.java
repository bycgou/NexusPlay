package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.CategoryMapper;
import com.biliplus.pojo.entity.Category;
import com.biliplus.service.Impl.CategoryServiceImpl;
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
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void listAll_shouldReturnMapperResult() {
        Category c = new Category();
        c.setId(1);
        c.setName("Music");
        when(categoryMapper.listAll()).thenReturn(List.of(c));

        List<Category> result = categoryService.listAll();

        assertEquals(1, result.size());
        assertEquals("Music", result.get(0).getName());
        verify(categoryMapper).listAll();
    }

    @Test
    void listByType_shouldFilterLive() {
        Category live = new Category();
        live.setType(2);
        live.setName("Game");
        when(categoryMapper.listByType(2)).thenReturn(List.of(live));

        List<Category> result = categoryService.listByType(2);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getType());
        verify(categoryMapper).listByType(2);
    }

    @Test
    void create_whenNameBlank_shouldThrow() {
        Category c = new Category();
        c.setName("  ");
        assertThrows(BusinessException.class, () -> categoryService.create(c));
        verify(categoryMapper, never()).insert(any());
    }

    @Test
    void create_shouldFillDefaultsAndInsert() {
        Category c = new Category();
        c.setName("Dance");

        categoryService.create(c);

        assertEquals(0, c.getParentId());
        assertEquals(0, c.getSortOrder());
        assertEquals(1, c.getType());
        assertNotNull(c.getCreateTime());
        verify(categoryMapper).insert(c);
    }

    @Test
    void create_liveCategory_shouldKeepType2() {
        Category c = new Category();
        c.setName("Chat");
        c.setType(2);

        categoryService.create(c);
        assertEquals(2, c.getType());
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        when(categoryMapper.selectById(99)).thenReturn(null);
        Category body = new Category();
        body.setName("new");
        assertThrows(BusinessException.class, () -> categoryService.update(99, body));
    }

    @Test
    void delete_whenHasVideos_shouldThrow() {
        Category existing = new Category();
        existing.setId(3);
        existing.setType(1);
        when(categoryMapper.selectById(3)).thenReturn(existing);
        when(categoryMapper.countVideosByCategoryId(3)).thenReturn(5);

        assertThrows(BusinessException.class, () -> categoryService.delete(3));
        verify(categoryMapper, never()).deleteById(3);
    }

    @Test
    void delete_whenLiveHasRooms_shouldThrow() {
        Category existing = new Category();
        existing.setId(5);
        existing.setType(2);
        when(categoryMapper.selectById(5)).thenReturn(existing);
        when(categoryMapper.countLiveRoomsByCategoryId(5)).thenReturn(2);

        assertThrows(BusinessException.class, () -> categoryService.delete(5));
    }

    @Test
    void delete_whenEmpty_shouldDelete() {
        Category existing = new Category();
        existing.setId(4);
        existing.setType(1);
        when(categoryMapper.selectById(4)).thenReturn(existing);
        when(categoryMapper.countVideosByCategoryId(4)).thenReturn(0);

        categoryService.delete(4);
        verify(categoryMapper).deleteById(4);
    }
}

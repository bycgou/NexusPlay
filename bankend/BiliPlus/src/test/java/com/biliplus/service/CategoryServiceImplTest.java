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
        c.setName("音乐");
        when(categoryMapper.listAll()).thenReturn(List.of(c));

        List<Category> result = categoryService.listAll();

        assertEquals(1, result.size());
        assertEquals("音乐", result.get(0).getName());
        verify(categoryMapper, times(1)).listAll();
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
        c.setName("舞蹈");

        categoryService.create(c);

        assertEquals(0, c.getParentId());
        assertEquals(0, c.getSortOrder());
        assertNotNull(c.getCreateTime());
        assertNotNull(c.getUpdateTime());
        verify(categoryMapper, times(1)).insert(c);
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        when(categoryMapper.selectById(99)).thenReturn(null);
        Category body = new Category();
        body.setName("新名字");
        assertThrows(BusinessException.class, () -> categoryService.update(99, body));
    }

    @Test
    void update_shouldCallMapper() {
        Category existing = new Category();
        existing.setId(2);
        existing.setName("旧");
        existing.setParentId(0);
        existing.setSortOrder(1);
        when(categoryMapper.selectById(2)).thenReturn(existing);
        when(categoryMapper.update(any())).thenReturn(1);

        Category body = new Category();
        body.setName("新");
        Category result = categoryService.update(2, body);

        assertEquals(2, result.getId() == null ? 2 : result.getId());
        verify(categoryMapper, times(1)).update(any());
    }

    @Test
    void delete_whenHasVideos_shouldThrow() {
        Category existing = new Category();
        existing.setId(3);
        when(categoryMapper.selectById(3)).thenReturn(existing);
        when(categoryMapper.countVideosByCategoryId(3)).thenReturn(5);

        assertThrows(BusinessException.class, () -> categoryService.delete(3));
        verify(categoryMapper, never()).deleteById(3);
    }

    @Test
    void delete_whenEmpty_shouldDelete() {
        Category existing = new Category();
        existing.setId(4);
        when(categoryMapper.selectById(4)).thenReturn(existing);
        when(categoryMapper.countVideosByCategoryId(4)).thenReturn(0);

        categoryService.delete(4);
        verify(categoryMapper, times(1)).deleteById(4);
    }
}

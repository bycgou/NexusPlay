package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.CategoryMapper;
import com.biliplus.pojo.entity.Category;
import com.biliplus.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.listAll();
    }

    @Override
    public List<Category> listByType(Integer type) {
        if (type == null) {
            return categoryMapper.listAll();
        }
        return categoryMapper.listByType(type);
    }

    @Override
    public Category create(Category category) {
        if (category == null || !StringUtils.hasText(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        if (category.getName().length() > 50) {
            throw new BusinessException("分类名称不能超过50字符");
        }
        if (category.getParentId() == null) {
            category.setParentId(0);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getType() == null) {
            category.setType(1);
        }
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Integer id, Category category) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        if (category == null || !StringUtils.hasText(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        category.setId(id);
        if (category.getParentId() == null) {
            category.setParentId(existing.getParentId());
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(existing.getSortOrder());
        }
        if (category.getType() == null) {
            category.setType(existing.getType() == null ? 1 : existing.getType());
        }
        category.setUpdateTime(LocalDateTime.now());
        int rows = categoryMapper.update(category);
        if (rows <= 0) {
            throw new BusinessException("分类更新失败");
        }
        return categoryMapper.selectById(id);
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        if (existing.getType() != null && existing.getType() == 2) {
            int liveCount = categoryMapper.countLiveRoomsByCategoryId(id);
            if (liveCount > 0) {
                throw new BusinessException("该直播分区下仍有直播间，无法删除");
            }
        } else {
            int videoCount = categoryMapper.countVideosByCategoryId(id);
            if (videoCount > 0) {
                throw new BusinessException("该分类下仍有视频，无法删除");
            }
        }
        categoryMapper.deleteById(id);
    }
}

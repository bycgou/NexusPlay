package com.biliplus.service;

import com.biliplus.pojo.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> listAll();

    Category create(Category category);

    Category update(Integer id, Category category);

    void delete(Integer id);
}

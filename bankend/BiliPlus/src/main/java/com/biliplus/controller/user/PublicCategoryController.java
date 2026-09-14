package com.biliplus.controller.user;

import com.biliplus.pojo.entity.Category;
import com.biliplus.result.Result;
import com.biliplus.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 公开分类接口（首页分类栏） */
@RestController
@RequestMapping("/pp/categories")
public class PublicCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }
}

package com.biliplus.controller.admin;

import com.biliplus.pojo.entity.Category;
import com.biliplus.result.Result;
import com.biliplus.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /** 获取分类列表；type=2 为直播分区 */
    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.listByType(type));
    }

    /** 新增视频分类 */
    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        log.info("新增分类: {}", category);
        return Result.success(categoryService.create(category));
    }

    /** 修改视频分类 */
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Integer id, @RequestBody Category category) {
        log.info("修改分类 id={}, body={}", id, category);
        return Result.success(categoryService.update(id, category));
    }

    /** 删除视频分类 */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        log.info("删除分类 id={}", id);
        categoryService.delete(id);
        return Result.success("删除成功");
    }
}

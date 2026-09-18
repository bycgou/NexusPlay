package com.biliplus.controller.user;

import com.biliplus.pojo.entity.Anime;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.AnimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pp/anime")
public class AnimeController {

    @Autowired
    private AnimeService animeService;

    /** 番剧列表（公开） */
    @GetMapping
    public Result<PageResult> list(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(animeService.pageQuery(page, pageSize, status, keyword));
    }

    /** 番剧详情（公开） */
    @GetMapping("/{id}")
    public Result<Anime> detail(@PathVariable Long id) {
        try {
            return Result.success(animeService.getById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

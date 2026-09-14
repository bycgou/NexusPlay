package com.biliplus.controller.admin;

import com.biliplus.pojo.entity.Banner;
import com.biliplus.result.Result;
import com.biliplus.service.BannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/banners")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.success(bannerService.listAll());
    }

    @PostMapping
    public Result<Banner> create(@RequestBody Banner banner) {
        log.info("新增轮播图: {}", banner);
        try {
            return Result.success(bannerService.create(banner));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Banner> update(@PathVariable Long id, @RequestBody Banner banner) {
        log.info("修改轮播图 id={}, body={}", id, banner);
        try {
            return Result.success(bannerService.update(id, banner));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除轮播图 id={}", id);
        try {
            bannerService.delete(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

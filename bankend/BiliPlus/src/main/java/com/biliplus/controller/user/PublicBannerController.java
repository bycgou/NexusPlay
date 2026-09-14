package com.biliplus.controller.user;

import com.biliplus.pojo.entity.Banner;
import com.biliplus.result.Result;
import com.biliplus.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 公开轮播图接口（首页） */
@RestController
@RequestMapping("/pp/banners")
public class PublicBannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public Result<List<Banner>> listOnline() {
        return Result.success(bannerService.listOnline());
    }
}

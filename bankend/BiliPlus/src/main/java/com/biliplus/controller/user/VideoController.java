package com.biliplus.controller.user;

import com.biliplus.pojo.dto.userdto.VideoPageQueryDTO;
import com.biliplus.pojo.vo.GetListVideoVO;
import com.biliplus.pojo.vo.VideoUploadVO;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/pp/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;


    // 1.分页查询
    @GetMapping("/page")
    public Result<PageResult> pageQuery(VideoPageQueryDTO videoPageQueryDTO) {
        log.info("视频分页查询参数：{}", videoPageQueryDTO);
        PageResult pageResult = videoService.pageQuery(videoPageQueryDTO);

        log.info("视频分页查询结果：{}", pageResult);
        return Result.success(pageResult);
    }

    // 2.根据ID查询视频详情
    @GetMapping("/{videoId}")
    public Result<VideoUploadVO> getVideo(@PathVariable Long videoId){
        log.info("根据ID查询视频详情：{}", videoId);

        VideoUploadVO videoUploadVO = videoService.getVideo(videoId);
        log.info("查询结果：{}", videoUploadVO);
        return Result.success(videoUploadVO);
    }

    // 3.加载推荐视频
    @GetMapping("/recommend")
    public Result<PageResult> recommend(){
        log.info("加载推荐视频");
        PageResult pageResult = videoService.recommend();
        log.info("查询结果：{}", pageResult);
        return Result.success(pageResult);
    }


}

package com.biliplus.controller.admin;

import com.biliplus.pojo.entity.Video;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.AdminVideoService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/videos")
public class AdminVideoController {

    @Autowired
    private AdminVideoService adminVideoService;

    @Data
    public static class RejectBody {
        /** 不通过原因，必填 */
        private String reason;
    }

    /** 视频详情（审核页用，含待审） */
    @GetMapping("/{id}")
    public Result<Video> detail(@PathVariable Long id) {
        Video video = adminVideoService.getById(id);
        if (video == null) {
            return Result.error("视频不存在");
        }
        return Result.success(video);
    }

    /** 待审/按状态分页列表 */
    @GetMapping("/page")
    public Result<PageResult> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(adminVideoService.pageByStatus(status, page, pageSize));
    }

    /** 审核通过 */
    @PostMapping("/{id}/approve")
    public Result<String> approve(@PathVariable Long id) {
        log.info("审核通过视频 id={}", id);
        try {
            adminVideoService.approve(id);
            return Result.success("已通过");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 审核不通过（必须带原因） */
    @PostMapping("/{id}/reject")
    public Result<String> reject(@PathVariable Long id, @RequestBody RejectBody body) {
        log.info("审核不通过视频 id={}, reason={}", id, body == null ? null : body.getReason());
        try {
            adminVideoService.reject(id, body == null ? null : body.getReason());
            return Result.success("已标记为不通过");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 下架 */
    @PostMapping("/{id}/offline")
    public Result<String> offline(@PathVariable Long id) {
        log.info("下架视频 id={}", id);
        try {
            adminVideoService.offline(id);
            return Result.success("已下架");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

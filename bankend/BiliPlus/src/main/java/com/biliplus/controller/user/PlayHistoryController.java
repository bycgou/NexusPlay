package com.biliplus.controller.user;

import com.biliplus.pojo.entity.PlayHistory;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.PlayHistoryService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pp/play-history")
public class PlayHistoryController {

    @Autowired
    private PlayHistoryService playHistoryService;

    /** 上报播放进度（登录后调用，同视频覆盖） */
    @PostMapping
    public Result<String> record(@RequestBody Map<String, Object> body) {
        try {
            Long userId = UserContext.getCurrentUserId();
            Long videoId = toLong(body == null ? null : body.get("videoId"));
            Integer progressSec = toInt(body == null ? null : body.get("progressSec"));
            Integer durationSec = toInt(body == null ? null : body.get("durationSec"));
            playHistoryService.record(userId, videoId, progressSec, durationSec);
            return Result.success("ok");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<PageResult> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        try {
            return Result.success(playHistoryService.list(UserContext.getCurrentUserId(), page, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 单视频续播进度；未登录或无记录返回 data=null，前端按从头播放处理 */
    @GetMapping("/video/{videoId}")
    public Result<PlayHistory> progress(@PathVariable Long videoId) {
        try {
            return Result.success(playHistoryService.getProgress(UserContext.getCurrentUserId(), videoId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{videoId}")
    public Result<String> removeOne(@PathVariable Long videoId) {
        try {
            playHistoryService.removeOne(UserContext.getCurrentUserId(), videoId);
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping
    public Result<String> clear() {
        try {
            playHistoryService.clear(UserContext.getCurrentUserId());
            return Result.success("已清空");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new com.biliplus.exception.BusinessException("videoId 非法");
        }
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return (int) Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

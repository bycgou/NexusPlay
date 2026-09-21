package com.biliplus.controller.user;

import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.InteractionService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pp/interaction")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like/{videoId}")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long videoId) {
        Long userId = UserContext.getCurrentUserId();
        log.info("点赞操作: videoId={}, userId={}", videoId, userId);
        try {
            Map<String, Object> result = interactionService.toggleLike(videoId, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return Result.error("操作失败");
        }
    }

    /**
     * 收藏/取消收藏。body 可选 {folderId}；缺省进默认收藏夹
     */
    @PostMapping("/favorite/{videoId}")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Long videoId,
                                                      @RequestBody(required = false) Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        Long folderId = null;
        if (body != null && body.get("folderId") != null) {
            Object raw = body.get("folderId");
            folderId = raw instanceof Number ? ((Number) raw).longValue()
                    : Long.parseLong(String.valueOf(raw));
        }
        log.info("收藏操作: videoId={}, userId={}, folderId={}", videoId, userId, folderId);
        try {
            Map<String, Object> result = interactionService.toggleFavorite(videoId, userId, folderId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("收藏操作失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 关注/取消关注
     */
    @PostMapping("/follow/{userId}")
    public Result<Map<String, Object>> toggleFollow(@PathVariable Long userId) {
        Long followerId = UserContext.getCurrentUserId();
        log.info("关注操作: followerId={}, followingId={}", followerId, userId);
        try {
            Map<String, Object> result = interactionService.toggleFollow(userId, followerId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("关注操作失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取视频互动状态（是否已点赞/收藏）
     */
    @GetMapping("/video/{videoId}")
    public Result<Map<String, Object>> getVideoInteractionStatus(@PathVariable Long videoId) {
        Long userId = UserContext.getCurrentUserId();
        Map<String, Object> result = interactionService.getVideoInteractionStatus(videoId, userId);
        return Result.success(result);
    }

    /**
     * 获取用户互动状态（是否已关注）
     */
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserInteractionStatus(@PathVariable Long userId) {
        Long currentUserId = UserContext.getCurrentUserId();
        Map<String, Object> result = interactionService.getUserInteractionStatus(currentUserId, userId);
        return Result.success(result);
    }

    /**
     * 我的点赞视频列表
     */
    @GetMapping("/my/liked")
    public Result<PageResult> listMyLikedVideos(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        log.info("查询我的点赞列表 userId={}, page={}, pageSize={}", userId, page, pageSize);
        try {
            return Result.success(interactionService.listLikedVideos(userId, page, pageSize));
        } catch (Exception e) {
            log.error("查询点赞列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 我的收藏视频列表
     */
    @GetMapping("/my/favorite")
    public Result<PageResult> listMyFavoriteVideos(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        log.info("查询我的收藏列表 userId={}, page={}, pageSize={}", userId, page, pageSize);
        try {
            return Result.success(interactionService.listFavoriteVideos(userId, page, pageSize));
        } catch (Exception e) {
            log.error("查询收藏列表失败", e);
            return Result.error(e.getMessage());
        }
    }
}

package com.biliplus.controller.user;

import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.DynamicService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pp/dynamics")
public class DynamicController {

    @Autowired
    private DynamicService dynamicService;

    /** 发文字动态 */
    @PostMapping
    public Result<?> publish(@RequestBody Map<String, Object> body) {
        try {
            Long userId = UserContext.getCurrentUserId();
            String content = body == null ? null : (String) body.get("content");
            return Result.success(dynamicService.publishText(userId, content));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 关注流：我关注的人 + 我自己 */
    @GetMapping("/feed")
    public Result<PageResult> feed(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        try {
            return Result.success(dynamicService.feed(UserContext.getCurrentUserId(), page, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 全站动态广场（公开，登录时带点赞态） */
    @GetMapping("/hot")
    public Result<PageResult> hot(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "20") Integer size) {
        try {
            return Result.success(dynamicService.hot(UserContext.getCurrentUserId(), page, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 某人的动态 */
    @GetMapping("/user/{userId}")
    public Result<PageResult> byUser(@PathVariable Long userId,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "20") Integer size) {
        try {
            return Result.success(dynamicService.byUser(UserContext.getCurrentUserId(), userId, page, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public Result<?> like(@PathVariable Long id) {
        try {
            return Result.success(dynamicService.toggleLike(UserContext.getCurrentUserId(), id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            dynamicService.delete(UserContext.getCurrentUserId(), id);
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

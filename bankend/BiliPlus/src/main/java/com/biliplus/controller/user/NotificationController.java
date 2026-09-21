package com.biliplus.controller.user;

import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.NotificationService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pp/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) Integer isRead,
                                   @RequestParam(required = false) Integer type,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(notificationService.list(userId, isRead, type, page, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 未读红点数字；未登录返回 0 而不是 401，便于 Header 静默轮询 */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.unreadCount(UserContext.getCurrentUserId()));
    }

    @PostMapping("/{id}/read")
    public Result<String> markRead(@PathVariable Long id) {
        try {
            notificationService.markRead(UserContext.getCurrentUserId(), id);
            return Result.success("ok");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/read-all")
    public Result<String> markAllRead() {
        try {
            notificationService.markAllRead(UserContext.getCurrentUserId());
            return Result.success("ok");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

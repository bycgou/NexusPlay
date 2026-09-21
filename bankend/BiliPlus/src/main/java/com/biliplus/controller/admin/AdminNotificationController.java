package com.biliplus.controller.admin;

import com.biliplus.result.Result;
import com.biliplus.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/notifications")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    /** 发送系统通知给全站正常用户 */
    @PostMapping
    public Result<String> send(@RequestBody Map<String, String> body) {
        try {
            String title = body == null ? null : body.get("title");
            String content = body == null ? null : body.get("content");
            notificationService.notifyAll(title, content);
            return Result.success("已发送");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

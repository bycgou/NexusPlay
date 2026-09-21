package com.biliplus.controller.user;

import com.biliplus.result.Result;
import com.biliplus.service.ReportService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pp/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /** 提交举报：{targetType, targetId, reason, detail?} */
    @PostMapping
    public Result<?> submit(@RequestBody Map<String, Object> body) {
        try {
            Long reporterId = UserContext.getCurrentUserId();
            Integer targetType = toInt(body == null ? null : body.get("targetType"));
            Long targetId = toLong(body == null ? null : body.get("targetId"));
            Integer reason = toInt(body == null ? null : body.get("reason"));
            String detail = body == null ? null : (String) body.get("detail");
            return Result.success(reportService.submit(reporterId, targetType, targetId, reason, detail));
        } catch (Exception e) {
            return Result.error(e.getMessage());
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
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
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
            return null;
        }
    }
}

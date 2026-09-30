package com.biliplus.controller.admin;

import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.SensitiveWordService;
import com.biliplus.service.UserPenaltyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 敏感词命中记录与误伤复核 */
@Slf4j
@RestController
@RequestMapping("/admin/sensitive-hits")
public class AdminSensitiveHitController {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private UserPenaltyService userPenaltyService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) Integer reviewStatus,
                                   @RequestParam(required = false) String action,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(sensitiveWordService.hitList(reviewStatus, action, page, size));
    }

    /** 命中统计：误伤率、类型分布、Top 词 */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.success(sensitiveWordService.hitSummary());
    }

    /** 复核 body:{reviewStatus: 1确认违规 | 2误伤} */
    @PostMapping("/{id}/review")
    public Result<String> review(@PathVariable Long id,
                                 @RequestBody Map<String, Object> body,
                                 @RequestAttribute("currentAdminId") Long adminId) {
        try {
            Integer reviewStatus = body == null || body.get("reviewStatus") == null
                    ? null : Integer.valueOf(String.valueOf(body.get("reviewStatus")));
            sensitiveWordService.reviewHit(adminId, id, reviewStatus);
            return Result.success("已复核");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ===== 处置记录与提前解除 =====

    @GetMapping("/penalties")
    public Result<PageResult> penalties(@RequestParam(required = false) Long userId,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(userPenaltyService.adminList(userId, status, page, size));
    }

    /** 对用户发起处置 body:{action:'mute'|'ban', reason, days(空=永久)} */
    @PostMapping("/penalties")
    public Result<?> penalize(@RequestBody Map<String, Object> body,
                              @RequestAttribute("currentAdminId") Long adminId) {
        try {
            Long userId = toLong(body == null ? null : body.get("userId"));
            String action = body == null ? null : (String) body.get("action");
            String reason = body == null ? null : (String) body.get("reason");
            Integer days = toInt(body == null ? null : body.get("days"));
            return Result.success(userPenaltyService.penalize(userId, action, reason, days, adminId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 提前解除 body:{action:'mute'|'ban'} */
    @PostMapping("/penalties/release")
    public Result<String> release(@RequestBody Map<String, Object> body,
                                  @RequestAttribute("currentAdminId") Long adminId) {
        try {
            Long userId = toLong(body == null ? null : body.get("userId"));
            String action = body == null ? null : (String) body.get("action");
            userPenaltyService.release(userId, action, adminId);
            return Result.success("已解除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long toLong(Object value) {
        return value == null ? null
                : (value instanceof Number ? ((Number) value).longValue() : Long.valueOf(String.valueOf(value)));
    }

    private Integer toInt(Object value) {
        return value == null ? null
                : (value instanceof Number ? ((Number) value).intValue() : Integer.valueOf(String.valueOf(value)));
    }
}

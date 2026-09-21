package com.biliplus.controller.admin;

import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Integer targetType,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(reportService.adminList(status, targetType, page, size));
    }

    /** 处理举报：{status:1成立|2驳回, remark?} */
    @PostMapping("/{id}/handle")
    public Result<String> handle(@PathVariable Long id,
                                 @RequestBody Map<String, Object> body,
                                 @RequestAttribute("currentAdminId") Long adminId) {
        try {
            Integer status = body == null || body.get("status") == null
                    ? null : Integer.valueOf(String.valueOf(body.get("status")));
            String remark = body == null ? null : (String) body.get("remark");
            reportService.handle(adminId, id, status, remark);
            return Result.success("已处理");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

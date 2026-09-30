package com.biliplus.controller.admin;

import com.biliplus.result.Result;
import com.biliplus.service.GovernanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 治理报表：论文第 6 章指标的数据来源 */
@Slf4j
@RestController
@RequestMapping("/admin/governance")
public class AdminGovernanceController {

    @Autowired
    private GovernanceService governanceService;

    /** 总览：举报时效、重复率、误伤率、复发率、低信用用户数 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(governanceService.overview());
    }

    /** 举报 SLA 与处理人工作量 */
    @GetMapping("/report-sla")
    public Result<Map<String, Object>> reportSla() {
        return Result.success(governanceService.reportSla());
    }

    /** 近 N 日处理时效趋势 */
    @GetMapping("/timeliness")
    public Result<Map<String, Object>> timeliness(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(governanceService.timelinessTrend(days == null ? 7 : days));
    }

    /** Top 违规用户（信用分最低） */
    @GetMapping("/top-violators")
    public Result<Map<String, Object>> topViolators(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(governanceService.topViolators(limit == null ? 10 : limit));
    }

    /** 违规曝光率：总量/占比/按天趋势，治理介入前后对比用 */
    @GetMapping("/violation-exposure")
    public Result<Map<String, Object>> violationExposure(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(governanceService.violationExposure(days == null ? 7 : days));
    }
}

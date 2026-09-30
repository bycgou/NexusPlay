package com.biliplus.controller.admin;

import com.biliplus.mapper.AdminOperationLogMapper;
import com.biliplus.pojo.entity.AdminOperationLog;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 管理端操作日志，只读 */
@Slf4j
@RestController
@RequestMapping("/admin/operation-logs")
public class AdminOperationLogController {

    @Autowired
    private AdminOperationLogMapper adminOperationLogMapper;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) Long adminId,
                                   @RequestParam(required = false) String action,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        List<AdminOperationLog> records =
                adminOperationLogMapper.adminList(adminId, action, (p - 1) * s, s);
        long total = adminOperationLogMapper.adminCount(adminId, action);
        return Result.success(new PageResult(total, records));
    }
}

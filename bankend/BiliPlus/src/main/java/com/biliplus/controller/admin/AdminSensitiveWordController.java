package com.biliplus.controller.admin;

import com.biliplus.pojo.entity.SensitiveWord;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.SensitiveWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/sensitive-words")
public class AdminSensitiveWordController {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(sensitiveWordService.adminList(keyword, status, page, size));
    }

    /** 新增敏感词 body:{word, level:1拦截 2转人工 3仅标记} */
    @PostMapping
    public Result<SensitiveWord> create(@RequestBody Map<String, Object> body) {
        try {
            String word = body == null ? null : (String) body.get("word");
            Integer level = body == null || body.get("level") == null
                    ? null : Integer.valueOf(String.valueOf(body.get("level")));
            return Result.success(sensitiveWordService.create(word, level));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<SensitiveWord> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Integer level = body == null || body.get("level") == null
                    ? null : Integer.valueOf(String.valueOf(body.get("level")));
            Integer status = body == null || body.get("status") == null
                    ? null : Integer.valueOf(String.valueOf(body.get("status")));
            return Result.success(sensitiveWordService.update(id, level, status));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            sensitiveWordService.delete(id);
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

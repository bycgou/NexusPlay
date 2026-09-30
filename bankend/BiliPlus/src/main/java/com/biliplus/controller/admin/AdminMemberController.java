package com.biliplus.controller.admin;

import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.AdminMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** C 端用户管理：列表 / 详情 / 禁言封禁 / 角色 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
public class AdminMemberController {

    @Autowired
    private AdminMemberService adminMemberService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Integer role,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(adminMemberService.list(keyword, status, role, page, size));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        try {
            return Result.success(adminMemberService.detail(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 封禁 body:{reason?, days? 空=永久} */
    @PostMapping("/{id}/ban")
    public Result<String> ban(@PathVariable Long id,
                              @RequestBody(required = false) Map<String, Object> body,
                              @RequestAttribute("currentAdminId") Long adminId) {
        try {
            String reason = body == null ? null : (String) body.get("reason");
            Integer days = toInt(body == null ? null : body.get("days"));
            adminMemberService.ban(id, reason, days, adminId);
            return Result.success("已封禁");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/unban")
    public Result<String> unban(@PathVariable Long id,
                                @RequestAttribute("currentAdminId") Long adminId) {
        try {
            adminMemberService.unban(id, adminId);
            return Result.success("已解封");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 禁言 body:{reason?, days? 空=永久} */
    @PostMapping("/{id}/mute")
    public Result<String> mute(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, Object> body,
                               @RequestAttribute("currentAdminId") Long adminId) {
        try {
            String reason = body == null ? null : (String) body.get("reason");
            Integer days = toInt(body == null ? null : body.get("days"));
            adminMemberService.mute(id, reason, days, adminId);
            return Result.success("已禁言");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/unmute")
    public Result<String> unmute(@PathVariable Long id,
                                 @RequestAttribute("currentAdminId") Long adminId) {
        try {
            adminMemberService.unmute(id, adminId);
            return Result.success("已解除禁言");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 调整角色 body:{role: 0普通/1UP/2管理员} */
    @PutMapping("/{id}/role")
    public Result<String> updateRole(@PathVariable Long id,
                                     @RequestBody Map<String, Object> body) {
        try {
            Integer role = toInt(body == null ? null : body.get("role"));
            adminMemberService.updateRole(id, role);
            return Result.success("角色已更新");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 该用户的禁言/封禁记录 */
    @GetMapping("/{id}/penalties")
    public Result<?> penalties(@PathVariable Long id) {
        try {
            return Result.success(adminMemberService.penalties(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Integer toInt(Object value) {
        return value == null ? null
                : (value instanceof Number ? ((Number) value).intValue() : Integer.valueOf(String.valueOf(value)));
    }
}

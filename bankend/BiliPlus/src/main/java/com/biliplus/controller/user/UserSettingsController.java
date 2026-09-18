package com.biliplus.controller.user;

import com.biliplus.pojo.dto.userdto.UserSettingsDTO;
import com.biliplus.result.Result;
import com.biliplus.service.UserSettingsService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pp/people/settings")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    /** 获取当前登录用户偏好设置 */
    @GetMapping
    public Result<UserSettingsDTO> getSettings() {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户偏好设置 userId={}", userId);
        try {
            return Result.success(userSettingsService.getSettings(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 保存/合并偏好设置（仅覆盖传入分类） */
    @PutMapping
    public Result<UserSettingsDTO> saveSettings(@RequestBody UserSettingsDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        log.info("保存用户偏好设置 userId={}", userId);
        try {
            return Result.success(userSettingsService.saveSettings(userId, dto));
        } catch (Exception e) {
            log.error("保存偏好设置失败", e);
            return Result.error(e.getMessage());
        }
    }

    /** 恢复默认（清空服务端配置） */
    @DeleteMapping
    public Result<Void> resetSettings() {
        Long userId = UserContext.getCurrentUserId();
        log.info("重置用户偏好设置 userId={}", userId);
        try {
            userSettingsService.resetSettings(userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

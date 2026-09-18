package com.biliplus.service;

import com.biliplus.pojo.dto.userdto.UserSettingsDTO;

public interface UserSettingsService {

    /** 获取当前用户偏好设置（无记录时返回空对象，由前端合并默认值） */
    UserSettingsDTO getSettings(Long userId);

    /** 保存/合并当前用户偏好设置（按分类覆盖） */
    UserSettingsDTO saveSettings(Long userId, UserSettingsDTO dto);

    /** 重置为系统默认（清空服务端记录，前端回落默认值） */
    void resetSettings(Long userId);
}

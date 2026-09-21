package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户偏好设置（播放/画质/通知/隐私/快捷键）
 * 对应数据库表：user_settings
 */
@Data
public class UserSettings {
    private Long id;
    private Long userId;
    /** 完整偏好 JSON 字符串 */
    private String settingsJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

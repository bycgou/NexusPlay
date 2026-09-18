package com.biliplus.pojo.dto.userdto;

import lombok.Data;

import java.util.Map;

/**
 * 用户偏好设置 DTO
 */
@Data
public class UserSettingsDTO {
    private Map<String, Object> player;
    private Map<String, Object> quality;
    private Map<String, Object> notify;
    private Map<String, Object> privacy;
    private Map<String, Object> shortcut;
}

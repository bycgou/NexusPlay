package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.UserSettingsMapper;
import com.biliplus.pojo.dto.userdto.UserSettingsDTO;
import com.biliplus.pojo.entity.UserSettings;
import com.biliplus.service.UserSettingsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserSettingsServiceImpl implements UserSettingsService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private UserSettingsMapper userSettingsMapper;

    @Override
    public UserSettingsDTO getSettings(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        UserSettings entity = userSettingsMapper.selectByUserId(userId);
        UserSettingsDTO dto = new UserSettingsDTO();
        if (entity == null || entity.getSettingsJson() == null || entity.getSettingsJson().isBlank()) {
            return dto;
        }
        try {
            Map<String, Object> map = MAPPER.readValue(
                    entity.getSettingsJson(),
                    new TypeReference<Map<String, Object>>() {}
            );
            dto.setPlayer(asMap(map.get("player")));
            dto.setQuality(asMap(map.get("quality")));
            dto.setNotify(asMap(map.get("notify")));
            dto.setPrivacy(asMap(map.get("privacy")));
            dto.setShortcut(asMap(map.get("shortcut")));
        } catch (Exception e) {
            log.warn("解析用户偏好设置失败 userId={}, err={}", userId, e.getMessage());
        }
        return dto;
    }

    @Override
    public UserSettingsDTO saveSettings(Long userId, UserSettingsDTO dto) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (dto == null) {
            return getSettings(userId);
        }

        Map<String, Object> merged = toMap(getSettings(userId));
        // 仅覆盖传入的分类
        if (dto.getPlayer() != null) {
            merged.put("player", dto.getPlayer());
        }
        if (dto.getQuality() != null) {
            merged.put("quality", dto.getQuality());
        }
        if (dto.getNotify() != null) {
            merged.put("notify", dto.getNotify());
        }
        if (dto.getPrivacy() != null) {
            merged.put("privacy", dto.getPrivacy());
        }
        if (dto.getShortcut() != null) {
            merged.put("shortcut", dto.getShortcut());
        }

        String json;
        try {
            json = MAPPER.writeValueAsString(merged);
        } catch (Exception e) {
            throw new BusinessException("设置序列化失败");
        }

        UserSettings existing = userSettingsMapper.selectByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            UserSettings entity = new UserSettings();
            entity.setUserId(userId);
            entity.setSettingsJson(json);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            userSettingsMapper.insert(entity);
        } else {
            userSettingsMapper.updateByUserId(userId, json, now);
        }

        return fromMap(merged);
    }

    @Override
    public void resetSettings(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        UserSettings existing = userSettingsMapper.selectByUserId(userId);
        if (existing != null) {
            userSettingsMapper.updateByUserId(userId, "{}", LocalDateTime.now());
        }
    }

    private Map<String, Object> asMap(Object obj) {
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) obj;
            return casted;
        }
        return null;
    }

    private Map<String, Object> toMap(UserSettingsDTO dto) {
        Map<String, Object> map = new HashMap<>();
        if (dto.getPlayer() != null) {
            map.put("player", dto.getPlayer());
        }
        if (dto.getQuality() != null) {
            map.put("quality", dto.getQuality());
        }
        if (dto.getNotify() != null) {
            map.put("notify", dto.getNotify());
        }
        if (dto.getPrivacy() != null) {
            map.put("privacy", dto.getPrivacy());
        }
        if (dto.getShortcut() != null) {
            map.put("shortcut", dto.getShortcut());
        }
        return map;
    }

    private UserSettingsDTO fromMap(Map<String, Object> map) {
        UserSettingsDTO dto = new UserSettingsDTO();
        dto.setPlayer(asMap(map.get("player")));
        dto.setQuality(asMap(map.get("quality")));
        dto.setNotify(asMap(map.get("notify")));
        dto.setPrivacy(asMap(map.get("privacy")));
        dto.setShortcut(asMap(map.get("shortcut")));
        return dto;
    }
}

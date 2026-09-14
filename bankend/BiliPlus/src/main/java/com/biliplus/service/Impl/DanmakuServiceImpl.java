package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.DanmakuMapper;
import com.biliplus.pojo.dto.userdto.DanmakuSendDTO;
import com.biliplus.pojo.entity.Danmaku;
import com.biliplus.service.DanmakuService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DanmakuServiceImpl implements DanmakuService {

    @Autowired
    private DanmakuMapper danmakuMapper;

    @Override
    public void saveDanmaku(DanmakuSendDTO danmakuSendDTO) {
        log.info("Service层保存弹幕：{}", danmakuSendDTO);
        if (danmakuSendDTO == null) {
            throw new BusinessException("弹幕数据为空");
        }
        if (danmakuSendDTO.getTime() == null || danmakuSendDTO.getTime() < 0 || danmakuSendDTO.getTime() > 86400) {
            throw new BusinessException("弹幕时间错误");
        }
        if (danmakuSendDTO.getText() == null || danmakuSendDTO.getText().trim().isEmpty()) {
            throw new BusinessException("弹幕内容不能为空");
        }
        if (danmakuSendDTO.getVideoId() == null) {
            throw new BusinessException("缺少视频ID");
        }

        // 身份以 JWT 为准，忽略客户端传入的 userId
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录再发送弹幕");
        }

        String color = normalizeColor(danmakuSendDTO.getColor());

        // 后端约定：1-滚动 2-顶部 3-底部
        String rawType = danmakuSendDTO.getType();
        Byte type;
        if (rawType == null) {
            type = 1;
        } else {
            type = switch (rawType.trim().toLowerCase()) {
                case "top", "2" -> 2;
                case "bottom", "3" -> 3;
                case "right", "scroll", "1", "0" -> 1;
                default -> 1;
            };
        }

        Danmaku danmaku = new Danmaku();
        danmaku.setVideoId(danmakuSendDTO.getVideoId());
        danmaku.setUserId(userId);
        danmaku.setContent(danmakuSendDTO.getText().trim());
        danmaku.setTime(danmakuSendDTO.getTime());
        danmaku.setColor(color);
        danmaku.setType(type);
        danmaku.setStatus((byte) 1);
        danmaku.setCreateTime(LocalDateTime.now());

        danmakuMapper.save(danmaku);
        log.info("弹幕已保存 videoId={}, userId={}, text={}", danmaku.getVideoId(), userId, danmaku.getContent());
    }

    /**
     * 兼容 DPlayer 发来的十进制 RGB（如 16777215）或 #FFFFFF / FFFFFF
     */
    private static String normalizeColor(String raw) {
        if (raw == null || raw.isBlank()) {
            return "FFFFFF";
        }
        String color = raw.trim();
        if (color.startsWith("#")) {
            color = color.substring(1);
        }
        // 十进制 RGB → 6 位 HEX
        if (color.matches("\\d{1,8}")) {
            try {
                int rgb = Integer.parseInt(color);
                color = String.format("%06X", rgb & 0xFFFFFF);
            } catch (NumberFormatException ignored) {
                color = "FFFFFF";
            }
        }
        if (!color.matches("[0-9A-Fa-f]{6}")) {
            color = "FFFFFF";
        }
        return color.toUpperCase();
    }

    @Override
    public List<Danmaku> getDanmakuById(Long videoId, int maxCount) {
        if (maxCount <= 0 || maxCount > 1000) {
            maxCount = 500;
        }
        List<Danmaku> danmakuList = danmakuMapper.getDanmakuById(videoId, maxCount);
        log.info("获取视频ID为 {} 的弹幕：共 {} 条", videoId, danmakuList == null ? 0 : danmakuList.size());
        return danmakuList;
    }
}

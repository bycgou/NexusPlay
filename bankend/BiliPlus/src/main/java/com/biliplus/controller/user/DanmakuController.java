package com.biliplus.controller.user;

import com.biliplus.pojo.dto.userdto.DanmakuSendDTO;
import com.biliplus.pojo.entity.Danmaku;
import com.biliplus.result.Result;
import com.biliplus.service.DanmakuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/pp/user/danmakuv3")
@RestController
public class DanmakuController {

    @Autowired
    private DanmakuService danmakuService;

    /**
     * DPlayer v3 弹幕协议：
     * GET /pp/user/danmakuv3/v3/?id=xx
     * 返回 { code: 0, data: [ [time, type, color, author, text], ... ] }
     * type: 1=滚动 5=顶部 4=底部
     * 注意：DPlayer 请求带尾斜杠 v3/，Spring Boot 3 默认不匹配 trailing slash
     */
    @GetMapping({"/v3", "/v3/"})
    public Map<String, Object> dplayerV3(@RequestParam("id") Long videoId) {
        List<Danmaku> list = danmakuService.getDanmakuById(videoId, 500);
        List<List<Object>> data = list.stream().map(danmaku -> {
            List<Object> row = new java.util.ArrayList<>(5);
            row.add(danmaku.getTime() == null ? 0 : danmaku.getTime());
            row.add(toDplayerV3Type(danmaku.getType()));
            row.add(normalizeColor(danmaku.getColor()));
            row.add("user-" + danmaku.getUserId());
            row.add(danmaku.getContent());
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("code", 0);
        body.put("data", data);
        log.info("DPlayer 拉取弹幕 videoId={}, count={}", videoId, data.size());
        return body;
    }

    /** 兼容 DPlayer 默认 POST {api}v3/（若未走自定义 apiBackend） */
    @PostMapping({"/v3", "/v3/"})
    public Map<String, Object> dplayerSendV3(@RequestBody Map<String, Object> body) {
        try {
            DanmakuSendDTO dto = new DanmakuSendDTO();
            Object id = body.get("id");
            if (id != null) {
                dto.setVideoId(Long.valueOf(String.valueOf(id)));
            }
            Object text = body.get("text");
            if (text != null) {
                dto.setText(String.valueOf(text));
            }
            Object time = body.get("time");
            if (time != null) {
                dto.setTime((int) Math.floor(Double.parseDouble(String.valueOf(time))));
            }
            Object color = body.get("color");
            dto.setColor(color == null ? null : String.valueOf(color));
            Object type = body.get("type");
            dto.setType(type == null ? null : String.valueOf(type));

            danmakuService.saveDanmaku(dto);
            Map<String, Object> ok = new HashMap<>();
            ok.put("code", 0);
            ok.put("data", new HashMap<>());
            return ok;
        } catch (Exception e) {
            log.warn("DPlayer 发送弹幕失败: {}", e.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("code", 1);
            err.put("data", new HashMap<>());
            return err;
        }
    }

    /** DPlayer/bilibili v3 数字类型 */
    private static int toDplayerV3Type(Byte type) {
        if (type == null) {
            return 1;
        }
        return switch (type) {
            case 2 -> 5; // 顶部
            case 3 -> 4; // 底部
            default -> 1; // 滚动
        };
    }

    /**
     * 兼容：/pp/user/danmakuv3/dplayer?id=xx（业务 Result 不用，给自定义拉取）
     */
    @GetMapping("/dplayer")
    public Map<String, Object> dplayerDanmaku(@RequestParam("id") Long videoId) {
        return dplayerV3(videoId);
    }

    private static String toDplayerType(Byte type) {
        if (type == null) {
            return "right";
        }
        return switch (type) {
            case 2 -> "top";
            case 3 -> "bottom";
            default -> "right";
        };
    }

    private static String normalizeColor(String color) {
        if (ObjectUtils.isEmpty(color)) {
            return "#FFFFFF";
        }
        String c = color.trim();
        if (!c.startsWith("#")) {
            c = "#" + c;
        }
        return c;
    }

    // 根据视频ID获取弹幕
    @GetMapping()
    public Result<List<Map<String, Object>>> getDanmaku(
            @RequestParam Long videoId,
            @RequestParam(defaultValue = "500") Integer maxCount) {
        log.info("获取视频ID为 {} 的弹幕, maxCount={}", videoId, maxCount);

        try {
            // 1. 调用Service层查询数据库
            List<Danmaku> list = danmakuService.getDanmakuById(videoId, maxCount);

            // 2. 将查询结果转换为前端（DPlayer）需要的格式
            List<Map<String, Object>> danmakuList = list.stream().map(danmaku -> {
                Map<String, Object> map = new HashMap<>();
                map.put("text", danmaku.getContent());
                map.put("time", danmaku.getTime()); // 注意：DPlayer的time单位是秒
                map.put("type", toDplayerType(danmaku.getType()));
                map.put("color", normalizeColor(danmaku.getColor()));
                return map;
            }).collect(Collectors.toList());

            return Result.success(danmakuList);

        } catch (Exception e) {
            log.error("获取弹幕失败：", e);
            return Result.error("获取弹幕失败");
        }
    }

    // 接受前端发送的弹幕
    @PostMapping()
    public Result<String> receiveDanmaku(@RequestBody DanmakuSendDTO danmakuSendDTO) {
        log.info("接收弹幕：videoId={}", danmakuSendDTO.getVideoId());
        try {
            danmakuService.saveDanmaku(danmakuSendDTO);
            return Result.success("弹幕接收成功");
        } catch (Exception e) {
            log.error("弹幕接收失败：{}", e.getMessage());
            return Result.error("弹幕接收失败");
        }
    }
}

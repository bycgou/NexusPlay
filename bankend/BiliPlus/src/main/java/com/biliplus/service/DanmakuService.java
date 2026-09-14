package com.biliplus.service;

import com.biliplus.pojo.dto.userdto.DanmakuSendDTO;
import com.biliplus.pojo.entity.Danmaku;

import java.util.List;

public interface DanmakuService {
    void saveDanmaku(DanmakuSendDTO danmakuSendDTO);

    /**
     * 获取视频弹幕
     * @param videoId 视频ID
     * @param maxCount 最大返回数量，默认500
     */
    List<Danmaku> getDanmakuById(Long videoId, int maxCount);
}

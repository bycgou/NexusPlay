package com.biliplus.service;

import com.biliplus.pojo.entity.PlayHistory;
import com.biliplus.result.PageResult;

public interface PlayHistoryService {

    /** 记录/更新播放进度 */
    void record(Long userId, Long videoId, Integer progressSec, Integer durationSec);

    PageResult list(Long userId, Integer page, Integer size);

    /** 单视频续播进度；未登录或没有记录时返回 null */
    PlayHistory getProgress(Long userId, Long videoId);

    void removeOne(Long userId, Long videoId);

    void clear(Long userId);
}

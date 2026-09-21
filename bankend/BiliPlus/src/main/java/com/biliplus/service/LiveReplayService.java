package com.biliplus.service;

import com.biliplus.pojo.entity.LiveReplay;
import com.biliplus.result.PageResult;

public interface LiveReplayService {

    /** 回放列表；userId 为空表示全站 */
    PageResult list(Long userId, Integer page, Integer size);

    LiveReplay getById(Long id);

    /**
     * 下播时登记回放。
     * 未配置录制（live.replay.enabled=false 或地址模板为空）时不写库，主流程不受影响。
     */
    void registerOnStop(com.biliplus.pojo.entity.LiveRoom room);
}

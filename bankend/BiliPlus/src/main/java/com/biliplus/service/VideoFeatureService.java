package com.biliplus.service;

/** 推荐池维护：过审入池，下架/驳回出池，周期刷新热度 */
public interface VideoFeatureService {

    void enterPool(Long videoId, Integer durationSec, Integer isOriginal);

    void leavePool(Long videoId);

    /** 补齐存量入池 + 下架出池，返回是否执行成功 */
    boolean ensurePool();

    /** 刷新在池视频热度分，返回更新条数 */
    int refreshHotScores();

    /** 定时任务入口：ensurePool + refreshHotScores */
    void scheduledRefresh();
}

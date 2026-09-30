package com.biliplus.service;

/** 推荐池维护：过审入池，下架/驳回出池 */
public interface VideoFeatureService {

    void enterPool(Long videoId, Integer durationSec, Integer isOriginal);

    void leavePool(Long videoId);
}

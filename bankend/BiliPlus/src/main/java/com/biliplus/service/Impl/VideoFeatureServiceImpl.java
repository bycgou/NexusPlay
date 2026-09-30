package com.biliplus.service.Impl;

import com.biliplus.mapper.VideoFeatureMapper;
import com.biliplus.service.VideoFeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VideoFeatureServiceImpl implements VideoFeatureService {

    @Autowired
    private VideoFeatureMapper videoFeatureMapper;

    @Override
    public void enterPool(Long videoId, Integer durationSec, Integer isOriginal) {
        if (videoId == null) {
            return;
        }
        try {
            videoFeatureMapper.enterPool(videoId, durationSec, isOriginal == null ? 1 : isOriginal);
            log.info("视频入推荐池 videoId={}", videoId);
        } catch (Exception e) {
            // 入池失败不阻断审核主流程
            log.warn("视频入推荐池失败 videoId={}", videoId, e);
        }
    }

    @Override
    public void leavePool(Long videoId) {
        if (videoId == null) {
            return;
        }
        try {
            videoFeatureMapper.leavePool(videoId);
        } catch (Exception e) {
            log.warn("视频出推荐池失败 videoId={}", videoId, e);
        }
    }
}

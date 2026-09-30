package com.biliplus.service.Impl;

import com.biliplus.mapper.VideoFeatureMapper;
import com.biliplus.service.VideoFeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Override
    public boolean ensurePool() {
        try {
            int inserted = videoFeatureMapper.ensurePool();
            int offline = videoFeatureMapper.markOffline();
            if (inserted > 0 || offline > 0) {
                log.info("推荐池对齐 video_feature 入池={} 出池={}", inserted, offline);
            }
            return true;
        } catch (Exception e) {
            log.warn("推荐池对齐失败", e);
            return false;
        }
    }

    @Override
    public int refreshHotScores() {
        try {
            int updated = videoFeatureMapper.refreshHotScores();
            log.debug("热度分刷新完成 count={}", updated);
            return updated;
        } catch (Exception e) {
            log.warn("热度分刷新失败", e);
            return 0;
        }
    }

    /** 每 5 分钟对齐推荐池并刷新热度，保证首页推荐/热榜可持续更新 */
    @Scheduled(fixedDelay = 300_000L, initialDelay = 20_000L)
    @Override
    public void scheduledRefresh() {
        ensurePool();
        refreshHotScores();
    }
}

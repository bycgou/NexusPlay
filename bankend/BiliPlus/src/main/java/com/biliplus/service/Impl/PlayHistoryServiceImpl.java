package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.PlayHistoryMapper;
import com.biliplus.pojo.entity.PlayHistory;
import com.biliplus.result.PageResult;
import com.biliplus.service.PlayHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlayHistoryServiceImpl implements PlayHistoryService {

    @Autowired
    private PlayHistoryMapper playHistoryMapper;

    @Override
    public void record(Long userId, Long videoId, Integer progressSec, Integer durationSec) {
        if (userId == null) {
            // 未登录不写历史，由前端只做本地进度记忆
            return;
        }
        if (videoId == null) {
            throw new BusinessException("视频ID不能为空");
        }
        PlayHistory history = new PlayHistory();
        history.setUserId(userId);
        history.setVideoId(videoId);
        history.setProgressSec(Math.max(0, progressSec == null ? 0 : progressSec));
        history.setDurationSec(Math.max(0, durationSec == null ? 0 : durationSec));
        playHistoryMapper.upsert(history);
    }

    @Override
    public PageResult list(Long userId, Integer page, Integer size) {
        requireLogin(userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        return new PageResult(
                playHistoryMapper.countByUser(userId),
                playHistoryMapper.pageByUser(userId, (p - 1) * s, s)
        );
    }

    @Override
    public PlayHistory getProgress(Long userId, Long videoId) {
        if (userId == null || videoId == null) {
            return null;
        }
        return playHistoryMapper.selectOne(userId, videoId);
    }

    @Override
    public void removeOne(Long userId, Long videoId) {
        requireLogin(userId);
        if (videoId == null) {
            throw new BusinessException("视频ID不能为空");
        }
        playHistoryMapper.deleteOne(userId, videoId);
    }

    @Override
    public void clear(Long userId) {
        requireLogin(userId);
        playHistoryMapper.deleteAll(userId);
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
    }
}

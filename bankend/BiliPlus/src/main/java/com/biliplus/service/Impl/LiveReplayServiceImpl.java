package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.LiveReplayMapper;
import com.biliplus.pojo.entity.LiveReplay;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.result.PageResult;
import com.biliplus.service.LiveReplayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class LiveReplayServiceImpl implements LiveReplayService {

    /** 回放转码完成前不可见，这里固定为可用 */
    private static final int STATUS_AVAILABLE = 1;

    @Autowired
    private LiveReplayMapper liveReplayMapper;

    /** 是否启用回放登记；无录制环境保持 false */
    @Value("${live.replay.enabled:false}")
    private boolean replayEnabled;

    /** 录制文件点播地址模板，{streamKey} 占位符会被替换 */
    @Value("${live.replay.play-url-template:}")
    private String playUrlTemplate;

    @Override
    public PageResult list(Long userId, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 12 : Math.min(size, 50);
        return new PageResult(
                liveReplayMapper.count(userId),
                liveReplayMapper.list(userId, (p - 1) * s, s)
        );
    }

    @Override
    public LiveReplay getById(Long id) {
        if (id == null) {
            throw new BusinessException("回放ID不能为空");
        }
        LiveReplay replay = liveReplayMapper.selectById(id);
        if (replay == null) {
            throw new BusinessException("回放不存在或已删除");
        }
        return replay;
    }

    @Override
    public void registerOnStop(LiveRoom room) {
        if (room == null || room.getId() == null || room.getUserId() == null) {
            return;
        }
        if (!replayEnabled || !StringUtils.hasText(playUrlTemplate)) {
            log.debug("回放未启用，跳过登记 roomId={}", room.getId());
            return;
        }
        String streamKey = room.getStreamKey();
        if (!StringUtils.hasText(streamKey)) {
            return;
        }

        Integer durationSec = null;
        if (room.getStartTime() != null && room.getEndTime() != null) {
            durationSec = (int) Math.max(0,
                    Duration.between(room.getStartTime(), room.getEndTime()).getSeconds());
        }

        LiveReplay replay = new LiveReplay();
        replay.setLiveRoomId(room.getId());
        replay.setUserId(room.getUserId());
        replay.setTitle(StringUtils.hasText(room.getTitle()) ? room.getTitle() : "直播回放");
        replay.setCoverUrl(room.getCoverUrl());
        replay.setPlayUrl(playUrlTemplate.replace("{streamKey}", streamKey));
        replay.setDurationSec(durationSec);
        replay.setStatus(STATUS_AVAILABLE);
        replay.setCreateTime(LocalDateTime.now());
        liveReplayMapper.insert(replay);
        log.info("登记直播回放 roomId={}, replayId={}, playUrl={}",
                room.getId(), replay.getId(), replay.getPlayUrl());
    }
}

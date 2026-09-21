package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 播放历史。(user_id, video_id) 唯一，同一视频只保留最近一次进度 */
@Data
public class PlayHistory {

    private Long id;
    private Long userId;
    private Long videoId;
    private Integer progressSec;
    private Integer durationSec;
    private LocalDateTime lastPlayTime;
    private LocalDateTime createTime;
}

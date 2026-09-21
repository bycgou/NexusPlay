package com.biliplus.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 播放历史列表项：历史进度 + 视频摘要 */
@Data
public class PlayHistoryVO {

    private Long id;
    private Long videoId;
    private Integer progressSec;
    private Integer durationSec;
    private LocalDateTime lastPlayTime;

    private String title;
    private String coverUrl;
    private Long userId;
    private String nickname;
}

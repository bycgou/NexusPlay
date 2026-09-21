package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 直播回放。无录制环境时不产生记录 */
@Data
public class LiveReplay {

    private Long id;
    private Long liveRoomId;
    /** 主播用户ID */
    private Long userId;
    private String title;
    private String coverUrl;
    /** 点播地址 */
    private String playUrl;
    private Integer durationSec;
    /** 1可用 0转码中 -1删除 */
    private Integer status;
    private LocalDateTime createTime;
}

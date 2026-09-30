package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 敏感词命中记录，用于复核误伤率 */
@Data
public class SensitiveHitLog {

    private Long id;
    private Long wordId;
    private String word;
    private Integer level;
    private Long userId;
    /** video|comment|danmaku|dynamic */
    private String targetType;
    private Long targetId;
    /** 命中片段 */
    private String content;
    /** block|mark */
    private String action;
    /** 0待复核 1确认违规 2误伤 */
    private Integer reviewStatus;
    private Long reviewAdminId;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
}

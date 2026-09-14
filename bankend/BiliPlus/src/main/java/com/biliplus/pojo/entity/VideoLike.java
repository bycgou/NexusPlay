package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频点赞记录（业务封装，底层表为 user_like，targetType=1）
 * 对应数据库表：user_like
 */
@Data
public class VideoLike {
    private Long id;
    private Long videoId;
    private Long userId;
    private LocalDateTime createTime;
}

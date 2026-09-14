package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 番剧剧集表实体类
 * 对应数据库表：anime_episode
 */
@Data
public class AnimeEpisode {

    /** 剧集ID（自增主键） */
    private Long id;

    /** 番剧ID */
    private Long animeId;

    /** 剧集标题 */
    private String title;

    /** 集数 */
    private Integer episodeNum;

    /** 视频URL */
    private String videoUrl;

    /** 时长（秒） */
    private Integer duration;

    /** 播放量 */
    private Integer viewCount;

    /** 发布时间 */
    private LocalDateTime releaseTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户追番表实体类
 * 对应数据库表：user_anime_follow
 */
@Data
public class UserAnimeFollow {

    /** ID（自增主键） */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 番剧ID */
    private Long animeId;

    /** 最新观看集数 */
    private Integer latestEpisode;

    /** 追番时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

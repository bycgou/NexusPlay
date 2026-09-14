package com.biliplus.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 番剧表实体类
 * 对应数据库表：anime
 */
@Data
public class Anime {

    /** 番剧ID（自增主键） */
    private Long id;

    /** 番剧标题 */
    private String title;

    /** 封面URL */
    private String coverUrl;

    /** 番剧描述 */
    private String description;

    /** 总集数 */
    private Integer totalEpisodes;

    /** 已播出集数 */
    private Integer airedEpisodes;

    /** 状态：0-未播出，1-连载中，2-已完结 */
    private Integer status;

    /** 评分 */
    private BigDecimal score;

    /** 追番数 */
    private Integer followerCount;

    /** 上映时间 */
    private LocalDate releaseTime;

    /** 完结时间 */
    private LocalDate endTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

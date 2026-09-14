package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户统计信息表实体类
 * 对应数据库表：user_statistics
 */
@Data
public class UserStatistics {

    /** ID（自增主键） */
    private Long id;

    /** 用户ID（唯一） */
    private Long userId;

    /** 粉丝数 */
    private Integer followerCount;

    /** 关注数 */
    private Integer followingCount;

    /** 视频数 */
    private Integer videoCount;

    /** 获赞数 */
    private Integer likeCount;

    /** 总播放量 */
    private Integer viewCount;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

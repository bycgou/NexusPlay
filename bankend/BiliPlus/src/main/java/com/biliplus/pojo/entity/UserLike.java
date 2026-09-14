package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户点赞表实体类
 * 对应数据库表：user_like
 */
@Data
public class UserLike {

    /** ID（自增主键） */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标ID（视频ID或评论ID） */
    private Long targetId;

    /** 目标类型：1-视频，2-评论 */
    private Integer targetType;

    /** 点赞时间 */
    private LocalDateTime createTime;
}

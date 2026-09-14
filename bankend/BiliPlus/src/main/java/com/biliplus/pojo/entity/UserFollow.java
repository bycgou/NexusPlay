package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注表实体类
 * 对应数据库表：user_follow
 */
@Data
public class UserFollow {

    /** ID（自增主键） */
    private Long id;

    /** 用户ID（关注者） */
    private Long userId;

    /** 被关注用户ID */
    private Long followUserId;

    /** 关注时间 */
    private LocalDateTime createTime;
}

package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播间表实体类
 * 对应数据库表：live_room
 */
@Data
public class LiveRoom {

    /** 直播间ID（自增主键） */
    private Long id;

    /** 直播标题 */
    private String title;

    /** 封面URL */
    private String coverUrl;

    /** 主播用户ID */
    private Long userId;

    /** 分类ID */
    private Integer categoryId;

    /** 状态：0-未开播，1-直播中，2-已结束 */
    private Integer status;

    /** 观看人数 */
    private Integer viewCount;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

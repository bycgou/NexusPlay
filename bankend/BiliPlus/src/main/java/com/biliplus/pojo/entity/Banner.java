package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页轮播图表实体类
 * 对应数据库表：banner
 */
@Data
public class Banner {

    private Long id;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 图片URL */
    private String imageUrl;

    /** 跳转类型：1-视频 2-外链 3-不跳转 */
    private Integer linkType;

    /** 关联视频ID（linkType=1） */
    private Long videoId;

    /** 外链（linkType=2） */
    private String linkUrl;

    /** 排序，越小越靠前 */
    private Integer sortOrder;

    /** 状态：0-下线 1-上线 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

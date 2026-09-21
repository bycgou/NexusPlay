package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 礼物目录 */
@Data
public class Gift {
    private Long id;
    private String name;
    private String iconUrl;
    /** 硬币价格 */
    private Integer price;
    /** 1普通 2中等 3全屏 */
    private Integer effectLevel;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
}

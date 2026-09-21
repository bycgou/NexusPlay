package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 收藏夹。每个用户有一个不可删除的默认收藏夹 */
@Data
public class FavoriteFolder {

    private Long id;
    private Long userId;
    private String name;
    /** 1为默认收藏夹（不可删除） */
    private Integer isDefault;
    /** 1私密 */
    private Integer isPrivate;
    private LocalDateTime createTime;
}

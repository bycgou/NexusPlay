package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签表实体类
 * 对应数据库表：tag
 */
@Data
public class Tag {

    /** 标签ID（自增主键） */
    private Long id;

    /** 标签名称（唯一） */
    private String name;

    /** 创建时间 */
    private LocalDateTime createTime;
}

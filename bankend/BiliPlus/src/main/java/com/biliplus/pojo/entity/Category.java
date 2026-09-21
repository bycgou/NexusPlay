package com.biliplus.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分类表实体类
 * 对应数据库表：category（用于内容/视频等资源的分类管理）
 */
@Data  // Lombok 组合注解：自动生成 Getter、Setter、toString、equals、hashCode 方法
public class Category {

    /**
     * 分类ID（自增主键）
     * 对应表字段：id (int NOT NULL AUTO_INCREMENT)
     */
    private Integer id;

    /**
     * 分类名称（非空，如"生活"、"游戏"）
     * 对应表字段：name (varchar(50) NOT NULL)
     */
    private String name;

    /**
     * 父分类ID（非空，默认0表示顶级分类）
     * 0：顶级分类；>0：子分类（关联当前表的id）
     * 对应表字段：parent_id (int NOT NULL DEFAULT 0)
     */
    private Integer parentId;

    /**
     * 排序序号（非空，默认0，值越小排序越靠前）
     * 用于控制分类在页面的展示顺序
     * 对应表字段：sort_order (int NOT NULL DEFAULT 0)
     */
    private Integer sortOrder;

    /**
     * 图标URL（可为空，如分类展示的图标路径）
     * 对应表字段：icon (varchar(255) DEFAULT NULL)
     */
    private String icon;

    /**
     * 分区类型：1-视频分区，2-直播分区
     * 对应表字段：type (tinyint NOT NULL DEFAULT 1)
     */
    private Integer type;

    /**
     * 创建时间（非空，默认当前时间）
     * 分类创建时自动填充，无需手动设置
     * 对应表字段：create_time (datetime NOT NULL DEFAULT CURRENT_TIMESTAMP)
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（非空，默认当前时间，更新时自动刷新）
     * 分类信息修改时自动更新，无需手动设置
     * 对应表字段：update_time (datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
     */
    private LocalDateTime updateTime;
}
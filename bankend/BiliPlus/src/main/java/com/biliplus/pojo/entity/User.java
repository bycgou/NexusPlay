package com.biliplus.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表实体类
 * 对应数据库表：user
 */
@Data  // Lombok 核心注解：自动生成 Getter、Setter、toString()、equals()、hashCode() 方法
public class User {

    /**
     * 用户ID（自增主键）
     * 对应表字段：id (bigint)
     */
    private Long id;

    /**
     * 用户名（非空，唯一）
     * 对应表字段：username (varchar(50))
     */
    private String username;

    /**
     * 加密密码（非空）
     * 对应表字段：password (varchar(100))
     */
    private String password;

    /**
     * 邮箱（可为空，索引）
     * 对应表字段：email (varchar(100))
     */
    private String email;

    /**
     * 手机号（可为空，索引）
     * 对应表字段：phone (varchar(20))
     */
    private String phone;

    /**
     * 头像URL（可为空）
     * 对应表字段：avatar (varchar(255))
     */
    private String avatar;

    /**
     * 昵称（可为空）
     * 对应表字段：nickname (varchar(50))
     */
    private String nickname;

    /**
     * 个性签名（可为空）
     * 对应表字段：signature (varchar(255))
     */
    private String signature;

    /**
     * 角色（非空，默认0）
     * 0-普通用户，1-UP主，2-管理员
     * 对应表字段：role (tinyint)
     */
    private Integer role;  // 注：表中是 tinyint，Java 用 Integer 兼容（避免 byte 类型的取值范围问题）

    /**
     * 状态（非空，默认1）
     * 0-禁用，1-正常
     * 对应表字段：status (tinyint)
     */
    private Integer status;  // 同 role，用 Integer 兼容 tinyint

    /**
     * 创建时间（非空，默认当前时间）
     * 对应表字段：create_time (datetime)
     * 注：Java 8+ 推荐用 LocalDateTime（替代 Date，支持时间格式化且线程安全）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（非空，默认当前时间，更新时自动刷新）
     * 对应表字段：update_time (datetime)
     */
    private LocalDateTime updateTime;
}

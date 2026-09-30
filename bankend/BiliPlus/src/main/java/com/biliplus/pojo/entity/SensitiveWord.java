package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 敏感词库 */
@Data
public class SensitiveWord {

    private Long id;
    private String word;
    /** 1拦截 2转人工 3仅标记 */
    private Integer level;
    /** 1启用 0停用 */
    private Integer status;
    private LocalDateTime createTime;
}

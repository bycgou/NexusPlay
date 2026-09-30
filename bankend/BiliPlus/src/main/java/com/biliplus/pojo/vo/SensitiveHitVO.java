package com.biliplus.pojo.vo;

import lombok.Data;

/** 敏感词命中结果（纯内存匹配，不含落库） */
@Data
public class SensitiveHitVO {

    private Long wordId;
    private String word;
    /** 1拦截 2转人工 3仅标记 */
    private Integer level;

    public boolean isBlocking() {
        return level != null && level == 1;
    }
}

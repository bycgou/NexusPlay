package com.biliplus.pojo.vo;

import lombok.Data;

/** 送礼结果（余额、连击、流水 ID） */
@Data
public class GiftSendResultVO {
    private Long recordId;
    private Long balance;
    private Integer combo;
    private Integer effectLevel;
}

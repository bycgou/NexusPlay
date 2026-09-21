package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 打赏流水 */
@Data
public class GiftRecord {
    private Long id;
    private Long liveRoomId;
    private Long pkId;
    private Long giftId;
    private Long senderId;
    private Long hostUserId;
    private Integer unitPrice;
    private Integer count;
    private Integer totalPrice;
    private LocalDateTime createTime;
}

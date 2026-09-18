package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 直播 PK */
@Data
public class LivePk {
    private Long id;
    private Long roomAId;
    private Long roomBId;
    private Long hostAId;
    private Long hostBId;
    /** 0邀请 1进行 2结束 3取消 */
    private Integer status;
    private Integer scoreA;
    private Integer scoreB;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSec;
    private LocalDateTime createTime;
}

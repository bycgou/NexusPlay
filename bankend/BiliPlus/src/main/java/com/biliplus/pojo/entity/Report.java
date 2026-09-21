package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 举报 */
@Data
public class Report {

    private Long id;
    private Long reporterId;
    /** 1视频 2评论 3弹幕 4用户 5直播间 */
    private Integer targetType;
    private Long targetId;
    /** 1违法 2色情 3辱骂 4广告 5其他 */
    private Integer reason;
    private String detail;
    /** 0待处理 1已处理 2已驳回 */
    private Integer status;
    /** 处理管理员ID */
    private Long handlerId;
    private String handleRemark;
    private LocalDateTime createTime;
    private LocalDateTime handleTime;
}

package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一行为流水。
 * 推荐特征、治理报表（含违规曝光率）、协同实验都从这张表取数，
 * 所以它是整个毕设的支点——只有聚合计数没有明细流水时，后续指标都算不出来。
 */
@Data
public class EventLog {

    private Long id;
    /** 未登录为 null */
    private Long userId;
    /** 见 constant.EventType */
    private String eventType;
    /** video|user|comment|live_room|gift|dynamic */
    private String targetType;
    private Long targetId;
    /** 观看/直播时长，秒 */
    private Integer durationSec;
    /** JSON 文本（分类ID、礼物金额、举报原因等扩展） */
    private String extra;
    /** web|admin|system */
    private String source;
    private LocalDateTime createTime;
}

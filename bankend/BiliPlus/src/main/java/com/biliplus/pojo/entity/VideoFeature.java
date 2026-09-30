package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 视频推荐特征 / 推荐池 */
@Data
public class VideoFeature {

    private Long videoId;
    private Integer durationSec;
    /** 1原创 0转载 */
    private Integer isOriginal;
    private Double hotScore;
    /** 1在池 0出池 */
    private Integer poolStatus;
    private LocalDateTime updateTime;
}

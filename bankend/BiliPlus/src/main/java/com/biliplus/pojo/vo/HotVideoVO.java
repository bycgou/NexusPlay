package com.biliplus.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 热榜条目：在推荐列表字段基础上附带排名与热度分 */
@Data
public class HotVideoVO {
    private Long id;
    private String title;
    private String coverUrl;
    private String duration;
    private Long userId;
    private Integer categoryId;
    private Long viewCount;
    private Long likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private LocalDateTime createTime;
    private String nickname;
    private String avatar;

    /** 热度分（与推荐打分同源） */
    private Double hotScore;

    /** 名次，从 1 开始 */
    private Integer rank;
}

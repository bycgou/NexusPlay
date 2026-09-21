package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户动态 */
@Data
public class Dynamic {

    private Long id;
    private Long userId;
    /** 1文字 2投稿视频 3转发 4开播 */
    private Integer type;
    private String content;
    private Long videoId;
    private Long originDynamicId;
    private Long liveRoomId;
    /** 1正常 0删除 */
    private Integer status;
    private LocalDateTime createTime;
}

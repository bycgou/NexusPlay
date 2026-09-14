package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class Danmaku {

    private Long id; // 弹幕id


    private Long videoId; // 视频id


    private Long userId; // 用户id


    private String content; // 弹幕内容


    private Integer time; // 单位：秒


    private String color; // 注意：你存的是 'FFFFFF'，不是 '#FFFFFF'


    private Byte type; // 1-滚动, 2-顶部, 3-底部


    private Byte status; // 0-屏蔽, 1-正常


    private LocalDateTime createTime;
}

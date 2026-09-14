package com.biliplus.pojo.dto.userdto;

import lombok.Data;
/*
 * 视频列表DTO
* 前端列表所需的视频数据格式
* */
@Data
public class VideoListDTO {
    private Long id;
    private String coverUrl;
    private String title;
    private String author; // 作者昵称
    private String duration;
    private Integer playCount; // 播放量（对应数据库viewCount）
    private Integer likeCount;
    private String tag; // 分类标签（如：原创、热门）
}

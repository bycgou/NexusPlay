package com.biliplus.pojo.dto.userdto;

import lombok.Data;

@Data
public class VideoUploadDTO {

    private Long userId; // 用户ID
    private String title; // 视频标题
    private String category; // 视频分类名称
    private Integer categoryId; // 视频分类ID
    private String type; // 创作类型(原创、转载)
    private String tags; // 视频标签,字符串数组
    private String description; // 视频简介
    private String coverUrl; // 封面图片的url
    private String videoUrl; // 视频文件的url
    private String duration; // 视频文件的时长
}

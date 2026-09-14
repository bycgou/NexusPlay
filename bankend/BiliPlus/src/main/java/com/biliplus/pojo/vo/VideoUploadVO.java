package com.biliplus.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoUploadVO {
    private Long id; // 视频ID (主键,自增)
    private String title; // 视频标题
    private String description; // 视频描述
    private String coverUrl; // 封面URL
    private String videoUrl; // 视频URL
    private Integer duration; // 视频时长,单位:秒
    private Long userId;  // 上传用户ID ,(关联用户表
    private Integer categoryId;  // 视频分类ID(非空,关联分类表)
    private Integer status;  // 视频状态
    private Long viewCount; // 播放次数
    private Long likeCount; // 点赞次数
    private Integer commentCount; // 评论次数
    private Integer shareCount; // 分享次数
    private String rejectReason; // 审核不通过原因
    private LocalDateTime createTime; // 创建时间
    private String nickname; // 用户昵称
    private String avatar; // 用户头像
}

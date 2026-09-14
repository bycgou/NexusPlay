package com.biliplus.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetListVideoVO {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private String duration;
    private Long userId;
    private Integer categoryId;
    private Integer status;
    private Long viewCount;
    private Long likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String nickname;
    private String avatar;
}

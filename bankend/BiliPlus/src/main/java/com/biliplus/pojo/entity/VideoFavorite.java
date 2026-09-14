package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoFavorite {
    private Long id;
    private Long videoId;
    private Long userId;
    private LocalDateTime createTime;
}

package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoFavorite {
    private Long id;
    private Long videoId;
    /** 收藏夹ID；为 NULL 表示历史数据，按默认收藏夹处理 */
    private Long folderId;
    private Long userId;
    private LocalDateTime createTime;
}

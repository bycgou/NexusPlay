package com.biliplus.pojo.entity;

import lombok.Data;

/**
 * 视频标签关联表实体类
 * 对应数据库表：video_tag
 */
@Data
public class VideoTag {

    /** ID（自增主键） */
    private Long id;

    /** 视频ID */
    private Long videoId;

    /** 标签ID */
    private Long tagId;
}

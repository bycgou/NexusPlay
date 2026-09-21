package com.biliplus.pojo.vo;

import com.biliplus.pojo.entity.Video;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 我的投稿列表项：视频字段 + 逗号分隔标签（标签存在 tag / video_tag 表） */
@Data
@EqualsAndHashCode(callSuper = true)
public class MyVideoVO extends Video {

    private String tags;
}

package com.biliplus.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 动态 Feed 卡片：动态本体 + 作者信息 + 关联视频信息 + 点赞态 */
@Data
public class DynamicVO {

    private Long id;
    private Long userId;
    private Integer type;
    private String content;
    private Long videoId;
    private Long liveRoomId;
    private LocalDateTime createTime;

    private String nickname;
    private String avatar;

    private String videoTitle;
    private String videoCoverUrl;

    private Long likeCount;
    /** 当前登录用户是否已点赞 */
    private Boolean liked;
}

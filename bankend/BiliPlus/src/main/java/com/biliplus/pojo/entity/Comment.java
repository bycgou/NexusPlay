package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {

    private Long id;

    private Long videoId;

    private Long userId;

    private String content;

    private Long parentId;

    private Integer likeCount;

    private Byte status; // 0-删除，1-正常

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 联表查询：评论用户昵称 */
    private String nickname;

    /** 联表查询：评论用户头像 */
    private String avatar;
}

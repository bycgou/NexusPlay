package com.biliplus.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 收藏夹列表项，带夹内视频数 */
@Data
public class FavoriteFolderVO {

    private Long id;
    private String name;
    private Integer isDefault;
    private Integer isPrivate;
    private LocalDateTime createTime;
    /** 夹内已收藏的视频数 */
    private Long videoCount;
}

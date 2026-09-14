package com.biliplus.pojo.dto.userdto;


/*
* 视频详情DTO
* 前端详情页所需的视频数据格式*/

import lombok.Data;

@Data
public class VideoDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private String duration;
    private Integer playCount;
    private String publishTime; // 发布时间（格式化后：yyyy-MM-dd）
    private AuthorDTO author; // 作者信息嵌套

    // 作者信息子DTO
    @Data
    public static class AuthorDTO {
        private String name; // 作者昵称
        private String avatar; // 头像URL
        private Integer fansCount; // 粉丝数
        private Long userId;
    }
}

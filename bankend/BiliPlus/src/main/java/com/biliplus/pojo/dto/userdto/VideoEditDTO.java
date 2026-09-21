package com.biliplus.pojo.dto.userdto;

import lombok.Data;

/** 用户编辑自有稿件可改的字段；视频源文件本期不支持修改 */
@Data
public class VideoEditDTO {

    private String title;

    private String description;

    private Integer categoryId;

    private String coverUrl;

    /** 逗号分隔的标签名，最多 10 个 */
    private String tags;
}

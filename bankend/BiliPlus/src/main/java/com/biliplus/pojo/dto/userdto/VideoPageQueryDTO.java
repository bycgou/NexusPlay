package com.biliplus.pojo.dto.userdto;

import lombok.Data;

@Data
public class VideoPageQueryDTO {
    /** 视频标题模糊搜索 */
    private String title;
    /** 分类ID筛选 */
    private Integer categoryId;
    /** 上传用户ID筛选（用户空间） */
    private Long userId;
    // 页码
    private Integer page;

    // 每页显示记录数
    private Integer pageSize;
}

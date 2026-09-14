package com.biliplus.pojo.dto.userdto;

import lombok.Data;

@Data
public class CommentPostDTO {
    private Long videoId;
    private String content;
    private Long parentId; // 0 表示顶级评论
    private Long commentId; // 删除评论时使用
}

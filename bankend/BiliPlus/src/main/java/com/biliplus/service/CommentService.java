package com.biliplus.service;

import com.biliplus.pojo.dto.userdto.CommentPostDTO;
import com.biliplus.result.PageResult;

public interface CommentService {
    void saveComment(CommentPostDTO commentPostDTO);

    PageResult getComment(Long videoId);

    void deleteComment(CommentPostDTO commentPostDTO);

    /** 评论点赞/取消点赞，返回是否已赞与最新点赞数 */
    java.util.Map<String, Object> toggleCommentLike(Long commentId, Long userId);
}

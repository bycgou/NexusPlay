package com.biliplus.controller.user;

import com.biliplus.pojo.dto.userdto.CommentPostDTO;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pp/comments")
@Slf4j
public class CommentController {


    @Autowired
    private CommentService commentService;

    //   添加评论
    @PostMapping()
    public Result<String> addComment(@RequestBody CommentPostDTO commentPostDTO){
        log.info("添加评论参数：{}", commentPostDTO);
        commentService.saveComment(commentPostDTO);
        return Result.success("添加成功");
    }

    // 获取评论
    @GetMapping ()
    public Result<PageResult> getComment(@RequestParam Long videoId){
        log.info("获取评论参数：{}",videoId );

        PageResult pageResult = commentService.getComment(videoId);
        log.info("获取结果：{}", pageResult);
        return Result.success(pageResult);
    }

    // 删除评论
    @DeleteMapping ()
    public Result<String> deleteComment(@RequestBody CommentPostDTO commentPostDTO){
        log.info("删除评论参数：{}", commentPostDTO);
        commentService.deleteComment(commentPostDTO);
        return Result.success("删除成功");
    }

    // 评论点赞/取消
    @PostMapping("/{commentId}/like")
    public Result<java.util.Map<String, Object>> toggleLike(@PathVariable Long commentId) {
        Long userId = com.biliplus.utils.UserContext.getCurrentUserId();
        log.info("评论点赞: commentId={}, userId={}", commentId, userId);
        try {
            return Result.success(commentService.toggleCommentLike(commentId, userId));
        } catch (com.biliplus.exception.BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

}

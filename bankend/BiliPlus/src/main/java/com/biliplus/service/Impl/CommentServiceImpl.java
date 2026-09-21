package com.biliplus.service.Impl;

import com.biliplus.constant.Notify;
import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.CommentMapper;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.mapper.UserLikeMapper;
import com.biliplus.mapper.VideoMapper;
import com.biliplus.pojo.dto.userdto.CommentPostDTO;
import com.biliplus.pojo.entity.Comment;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.entity.UserLike;
import com.biliplus.pojo.entity.Video;
import com.biliplus.result.PageResult;
import com.biliplus.service.CommentService;
import com.biliplus.service.NotificationService;
import com.biliplus.utils.UserContext;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    /** 评论点赞目标类型 */
    public static final Integer TARGET_TYPE_COMMENT = 2;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserLikeMapper userLikeMapper;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public void saveComment(CommentPostDTO commentPostDTO) {
        log.info("service层添加评论参数DTO：{}", commentPostDTO);
        if (commentPostDTO == null || commentPostDTO.getVideoId() == null
                || !StringUtils.hasText(commentPostDTO.getContent())) {
            throw new BusinessException("评论参数不完整");
        }
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        Comment comment = new Comment();
        comment.setVideoId(commentPostDTO.getVideoId());
        comment.setUserId(userId);
        comment.setContent(commentPostDTO.getContent().trim());
        long parentId = commentPostDTO.getParentId() == null ? 0L : commentPostDTO.getParentId();
        comment.setParentId(parentId);
        comment.setStatus((byte) 1);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        commentMapper.save(comment);
        videoMapper.increaseCommentCount(comment.getVideoId());

        notifyComment(comment, userId);
    }

    /** 顶级评论通知 UP 主，回复则通知被回复的评论作者 */
    private void notifyComment(Comment comment, Long userId) {
        long parentId = comment.getParentId() == null ? 0L : comment.getParentId();

        if (parentId > 0) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent != null) {
                notificationService.notify(parent.getUserId(), userId, Notify.TYPE_REPLY,
                        resolveNickname(userId) + " 回复了你的评论",
                        abbreviate(comment.getContent(), 40), Notify.BIZ_COMMENT, comment.getId());
            }
            return;
        }

        Video video = videoMapper.getVideo(comment.getVideoId());
        if (video != null) {
            notificationService.notify(video.getUserId(), userId, Notify.TYPE_COMMENT,
                    resolveNickname(userId) + " 评论了你的视频",
                    abbreviate(comment.getContent(), 40), Notify.BIZ_VIDEO, comment.getVideoId());
        }
    }

    private String resolveNickname(Long userId) {
        User user = peopleUserMapper.getUserById(userId);
        if (user == null) {
            return "用户" + userId;
        }
        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    private String abbreviate(String text, int max) {
        if (text == null) {
            return null;
        }
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= max ? flat : flat.substring(0, max) + "…";
    }

    @Override
    public PageResult getComment(Long videoId) {
        log.info("service层获取评论参数：{}", videoId);
        PageHelper.startPage(1, 100);
        Page<Comment> page = commentMapper.pageQuery(videoId);
        long total = page.getTotal();
        List<Comment> records = page.getResult();
        return new PageResult(total, records);
    }

    @Override
    @Transactional
    public void deleteComment(CommentPostDTO commentPostDTO) {
        log.info("service层删除评论参数DTO：{}", commentPostDTO);
        if (commentPostDTO == null || commentPostDTO.getVideoId() == null) {
            throw new BusinessException("缺少评论信息");
        }
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        // 删除：优先 commentId，兼容 parentId
        Long commentId = commentPostDTO.getCommentId() != null
                ? commentPostDTO.getCommentId()
                : commentPostDTO.getParentId();
        if (commentId == null || commentId <= 0) {
            throw new BusinessException("请指定要删除的评论ID");
        }
        Comment existing = commentMapper.selectById(commentId);
        if (existing == null || existing.getStatus() == null || existing.getStatus() == 0) {
            throw new BusinessException("评论不存在");
        }
        if (!userId.equals(existing.getUserId())) {
            throw new BusinessException("只能删除自己的评论");
        }
        commentMapper.softDelete(commentId, userId);
        videoMapper.decreaseCommentCount(existing.getVideoId());
    }

    @Override
    @Transactional
    public Map<String, Object> toggleCommentLike(Long commentId, Long userId) {
        if (commentId == null || commentId <= 0) {
            throw new BusinessException("评论ID不合法");
        }
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        Comment existing = commentMapper.selectById(commentId);
        if (existing == null || existing.getStatus() == null || existing.getStatus() == 0) {
            throw new BusinessException("评论不存在");
        }

        Map<String, Object> result = new HashMap<>();
        int count = userLikeMapper.countByUserAndTarget(userId, commentId, TARGET_TYPE_COMMENT);
        if (count > 0) {
            userLikeMapper.delete(userId, commentId, TARGET_TYPE_COMMENT);
            commentMapper.changeLikeCount(commentId, -1);
            result.put("liked", false);
        } else {
            UserLike like = new UserLike();
            like.setUserId(userId);
            like.setTargetId(commentId);
            like.setTargetType(TARGET_TYPE_COMMENT);
            like.setCreateTime(LocalDateTime.now());
            userLikeMapper.insert(like);
            commentMapper.changeLikeCount(commentId, 1);
            result.put("liked", true);
        }
        int likeCount = existing.getLikeCount() == null ? 0 : existing.getLikeCount();
        likeCount = Boolean.TRUE.equals(result.get("liked")) ? likeCount + 1 : Math.max(0, likeCount - 1);
        result.put("likeCount", likeCount);
        return result;
    }
}

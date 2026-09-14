package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.UserFollowMapper;
import com.biliplus.mapper.VideoFavoriteMapper;
import com.biliplus.mapper.VideoLikeMapper;
import com.biliplus.mapper.VideoMapper;
import com.biliplus.pojo.entity.UserFollow;
import com.biliplus.pojo.entity.VideoFavorite;
import com.biliplus.pojo.entity.VideoLike;
import com.biliplus.service.InteractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class InteractionServiceImpl implements InteractionService {

    @Autowired
    private VideoLikeMapper videoLikeMapper;

    @Autowired
    private VideoFavoriteMapper videoFavoriteMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long videoId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        int count = videoLikeMapper.countByVideoAndUser(videoId, userId);

        if (count > 0) {
            videoLikeMapper.delete(videoId, userId);
            videoMapper.changeLikeCount(videoId, -1);
            result.put("liked", false);
            log.info("取消点赞: videoId={}, userId={}", videoId, userId);
        } else {
            VideoLike videoLike = new VideoLike();
            videoLike.setVideoId(videoId);
            videoLike.setUserId(userId);
            videoLike.setCreateTime(LocalDateTime.now());
            videoLikeMapper.insert(videoLike);
            videoMapper.changeLikeCount(videoId, 1);
            result.put("liked", true);
            log.info("添加点赞: videoId={}, userId={}", videoId, userId);
        }

        result.put("likeCount", videoLikeMapper.countByVideoId(videoId));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> toggleFavorite(Long videoId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        int count = videoFavoriteMapper.countByVideoAndUser(videoId, userId);

        if (count > 0) {
            videoFavoriteMapper.delete(videoId, userId);
            result.put("collected", false);
            log.info("取消收藏: videoId={}, userId={}", videoId, userId);
        } else {
            VideoFavorite videoFavorite = new VideoFavorite();
            videoFavorite.setVideoId(videoId);
            videoFavorite.setUserId(userId);
            videoFavorite.setCreateTime(LocalDateTime.now());
            videoFavoriteMapper.insert(videoFavorite);
            result.put("collected", true);
            log.info("添加收藏: videoId={}, userId={}", videoId, userId);
        }

        result.put("favoriteCount", videoFavoriteMapper.countByVideoId(videoId));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> toggleFollow(Long followingId, Long followerId) {
        Map<String, Object> result = new HashMap<>();

        if (followerId == null) {
            throw new BusinessException("请先登录");
        }
        if (followingId.equals(followerId)) {
            throw new BusinessException("不能关注自己");
        }

        int count = userFollowMapper.countByPair(followerId, followingId);

        if (count > 0) {
            userFollowMapper.delete(followerId, followingId);
            result.put("followed", false);
            log.info("取消关注: userId={}, followUserId={}", followerId, followingId);
        } else {
            UserFollow userFollow = new UserFollow();
            userFollow.setUserId(followerId);
            userFollow.setFollowUserId(followingId);
            userFollow.setCreateTime(LocalDateTime.now());
            userFollowMapper.insert(userFollow);
            result.put("followed", true);
            log.info("添加关注: userId={}, followUserId={}", followerId, followingId);
        }

        result.put("fansCount", userFollowMapper.countFans(followingId));
        return result;
    }

    @Override
    public Map<String, Object> getVideoInteractionStatus(Long videoId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        boolean liked = userId != null && videoLikeMapper.countByVideoAndUser(videoId, userId) > 0;
        boolean collected = userId != null && videoFavoriteMapper.countByVideoAndUser(videoId, userId) > 0;

        result.put("liked", liked);
        result.put("collected", collected);
        result.put("likeCount", videoLikeMapper.countByVideoId(videoId));
        result.put("favoriteCount", videoFavoriteMapper.countByVideoId(videoId));

        return result;
    }

    @Override
    public Map<String, Object> getUserInteractionStatus(Long userId, Long targetUserId) {
        Map<String, Object> result = new HashMap<>();

        boolean followed = userId != null && targetUserId != null
                && userFollowMapper.countByPair(userId, targetUserId) > 0;

        result.put("followed", followed);
        result.put("fansCount", userFollowMapper.countFans(targetUserId));
        result.put("followingCount", userFollowMapper.countFollowing(targetUserId));

        return result;
    }
}

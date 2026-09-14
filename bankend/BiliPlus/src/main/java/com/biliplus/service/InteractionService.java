package com.biliplus.service;

import java.util.Map;

public interface InteractionService {

    /**
     * 点赞/取消点赞
     */
    Map<String, Object> toggleLike(Long videoId, Long userId);

    /**
     * 收藏/取消收藏
     */
    Map<String, Object> toggleFavorite(Long videoId, Long userId);

    /**
     * 关注/取消关注
     */
    Map<String, Object> toggleFollow(Long followingId, Long followerId);

    /**
     * 获取视频互动状态
     */
    Map<String, Object> getVideoInteractionStatus(Long videoId, Long userId);

    /**
     * 获取用户互动状态（是否关注）
     */
    Map<String, Object> getUserInteractionStatus(Long userId, Long targetUserId);
}

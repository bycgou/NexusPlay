package com.biliplus.service;

import com.biliplus.result.PageResult;

import java.util.Map;

public interface InteractionService {

    /**
     * 点赞/取消点赞
     */
    Map<String, Object> toggleLike(Long videoId, Long userId);

    /**
     * 收藏/取消收藏。
     * folderId 为空时进默认收藏夹；取消收藏忽略 folderId。
     */
    Map<String, Object> toggleFavorite(Long videoId, Long userId, Long folderId);

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

    /**
     * 我的点赞视频列表（分页）
     */
    PageResult listLikedVideos(Long userId, Integer page, Integer pageSize);

    /**
     * 我的收藏视频列表（分页）
     */
    PageResult listFavoriteVideos(Long userId, Integer page, Integer pageSize);
}

package com.biliplus.service;

import com.biliplus.result.PageResult;

import java.util.Map;

public interface DynamicService {

    /** 发布文字动态 */
    com.biliplus.pojo.entity.Dynamic publishText(Long userId, String content);

    /** 投稿审核通过后自动发一条投稿动态（同一视频只发一次） */
    void publishVideoDynamic(Long videoId);

    /** 关注流：我关注的人 + 我自己 */
    PageResult feed(Long userId, Integer page, Integer size);

    /** 全站动态广场 */
    PageResult hot(Long viewerId, Integer page, Integer size);

    /** 某人的动态 */
    PageResult byUser(Long viewerId, Long userId, Integer page, Integer size);

    /** 点赞开关 */
    Map<String, Object> toggleLike(Long userId, Long dynamicId);

    /** 作者本人删除 */
    void delete(Long userId, Long dynamicId);
}

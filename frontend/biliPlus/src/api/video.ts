// src/api/video.js
import request from '@/utils/request';

// 1. 获取视频列表（title / categoryId / userId 可选）
export const getVideoList = (params = {}) => {
    return request({
        url: '/pp/videos/page',
        method: 'get',
        params: {
            page: params.page || 1,
            pageSize: params.pageSize || 20,
            title: params.title,
            categoryId: params.categoryId,
            userId: params.userId
        }
    });
}

// 2. 获取视频详情
export const getVideoDetail = (videoId) => {
    return request({
        url: `/pp/videos/${videoId}`,
        method: 'get'
    });
}

// 3. 获取推荐视频
export const getRecommendVideos = (params = {}) => {
    return request({
        url: `/pp/videos/recommend`,
        method: 'get',
        params: {
            page: params.page || 1,
            size: params.size || 20
        }
    });
}

// 3b. 热搜/热榜 TopN
export const getHotVideos = (size = 20) => {
    return request({
        url: `/pp/videos/hot`,
        method: 'get',
        params: { size }
    });
}

// 4. 获取用户信息
export const getUserInfo = (userId) => {
    return request({
        url: `/pp/people/user/${userId}`,
        method: 'get'
    });
}

// 5. 分享计数（未登录也可调用，返回最新分享数）
export const shareVideo = (videoId) => {
    return request({
        url: `/pp/videos/${videoId}/share`,
        method: 'post'
    });
}

// 6. 视频弹幕列表（含 id/userId，供举报点选）
export const getVideoDanmakus = (videoId, maxCount = 500) => {
    return request({
        url: '/pp/user/danmakuv3',
        method: 'get',
        params: { videoId, maxCount }
    });
}

export {request}

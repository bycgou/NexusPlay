// src/api/video.js
import request from '@/utils/request'; // ✅ 导入你封装好的带拦截器的实例

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
export const getRecommendVideos = () => {
    return request({
        url: `/pp/videos/recommend`,
        method: 'get'
    });
}

// 4. 获取用户信息
export const getUserInfo = (userId) => {
    return request({
        url: `/pp/people/user/${userId}`,
        method: 'get'
    });
}

export {request}
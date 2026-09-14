import request from '@/utils/request'

// 点赞/取消点赞
export const toggleLike = (videoId: number) => {
    return request({
        url: `/pp/interaction/like/${videoId}`,
        method: 'post'
    })
}

// 收藏/取消收藏
export const toggleFavorite = (videoId: number) => {
    return request({
        url: `/pp/interaction/favorite/${videoId}`,
        method: 'post'
    })
}

// 关注/取消关注
export const toggleFollow = (userId: number) => {
    return request({
        url: `/pp/interaction/follow/${userId}`,
        method: 'post'
    })
}

// 获取视频互动状态
export const getVideoInteractionStatus = (videoId: number) => {
    return request({
        url: `/pp/interaction/video/${videoId}`,
        method: 'get'
    })
}

// 获取用户互动状态
export const getUserInteractionStatus = (userId: number) => {
    return request({
        url: `/pp/interaction/user/${userId}`,
        method: 'get'
    })
}

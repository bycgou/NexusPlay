import request from '@/utils/request'

// 点赞/取消点赞
export const toggleLike = (videoId: number) => {
    return request({
        url: `/pp/interaction/like/${videoId}`,
        method: 'post'
    })
}

// 收藏/取消收藏；传入 folderId 时收藏到指定收藏夹，缺省进默认收藏夹
export const toggleFavorite = (videoId: number, folderId?: number) => {
    return request({
        url: `/pp/interaction/favorite/${videoId}`,
        method: 'post',
        data: folderId ? { folderId } : {}
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

// 我的点赞视频列表
export const getMyLikedVideos = (page = 1, pageSize = 20) => {
    return request({
        url: '/pp/interaction/my/liked',
        method: 'get',
        params: { page, pageSize }
    })
}

// 我的收藏视频列表
export const getMyFavoriteVideos = (page = 1, pageSize = 20) => {
    return request({
        url: '/pp/interaction/my/favorite',
        method: 'get',
        params: { page, pageSize }
    })
}

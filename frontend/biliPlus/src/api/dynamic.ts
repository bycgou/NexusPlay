import request from '@/utils/request'

export interface DynamicItem {
    id: number
    userId: number
    /** 1文字 2投稿视频 3转发 4开播 */
    type: number
    content?: string
    videoId?: number
    liveRoomId?: number
    createTime?: string
    nickname?: string
    avatar?: string
    videoTitle?: string
    videoCoverUrl?: string
    likeCount?: number
    /** 当前登录用户是否已点赞 */
    liked?: boolean
}

export interface DynamicPageResult {
    total: number
    records: DynamicItem[]
}

// 关注流（我关注的人 + 我自己），需登录
export const getDynamicFeed = (params: { page?: number; size?: number } = {}) => {
    return request({
        url: '/pp/dynamics/feed',
        method: 'get',
        params: { page: 1, size: 20, ...params }
    })
}

// 全站动态广场，公开
export const getHotDynamics = (params: { page?: number; size?: number } = {}) => {
    return request({
        url: '/pp/dynamics/hot',
        method: 'get',
        params: { page: 1, size: 20, ...params }
    })
}

// 某人的动态
export const getUserDynamics = (userId: number, params: { page?: number; size?: number } = {}) => {
    return request({
        url: `/pp/dynamics/user/${userId}`,
        method: 'get',
        params: { page: 1, size: 20, ...params }
    })
}

// 发文字动态
export const publishDynamic = (content: string) => {
    return request({
        url: '/pp/dynamics',
        method: 'post',
        data: { content }
    })
}

// 点赞开关
export const toggleDynamicLike = (id: number) => {
    return request({
        url: `/pp/dynamics/${id}/like`,
        method: 'post'
    })
}

// 删除自己的动态
export const deleteDynamic = (id: number) => {
    return request({
        url: `/pp/dynamics/${id}`,
        method: 'delete'
    })
}

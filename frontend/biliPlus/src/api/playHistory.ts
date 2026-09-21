import request from '@/utils/request'

export interface PlayHistoryItem {
    id: number
    videoId: number
    progressSec: number
    durationSec: number
    lastPlayTime: string
    title: string
    coverUrl?: string
    userId: number
    nickname?: string
}

export interface PlayHistoryRecord {
    videoId: number
    progressSec: number
    durationSec: number
}

// 上报播放进度（登录后调用，同视频覆盖）
export const reportPlayProgress = (data: PlayHistoryRecord) => {
    return request({
        url: '/pp/play-history',
        method: 'post',
        data
    })
}

// 我的播放历史分页
export const listPlayHistory = (params: { page?: number; size?: number } = {}) => {
    return request({
        url: '/pp/play-history',
        method: 'get',
        params: { page: 1, size: 20, ...params }
    })
}

// 单视频续播进度；无记录时 data 为 null
export const getPlayProgress = (videoId: number | string) => {
    return request({
        url: `/pp/play-history/video/${videoId}`,
        method: 'get'
    })
}

// 删除单条历史
export const removePlayHistory = (videoId: number | string) => {
    return request({
        url: `/pp/play-history/${videoId}`,
        method: 'delete'
    })
}

// 清空历史
export const clearPlayHistory = () => {
    return request({
        url: '/pp/play-history',
        method: 'delete'
    })
}

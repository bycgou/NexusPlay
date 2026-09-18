import request from '@/utils/request'

export interface LiveRoom {
    id: number
    title: string
    coverUrl?: string
    userId: number
    hostNickname?: string
    hostAvatar?: string
    categoryId?: number
    status: number
    viewCount?: number
    startTime?: string
    endTime?: string
    streamKey?: string
    playUrl?: string
    pushUrl?: string
    onlineCount?: number
}

export interface PageResult {
    total: number
    records: LiveRoom[]
}

// 开播
export const startLive = (data: { title: string; coverUrl?: string; categoryId: number }) => {
    return request({
        url: '/pp/live/rooms',
        method: 'post',
        data
    })
}

// 下播
export const stopLive = (roomId: number) => {
    return request({
        url: `/pp/live/rooms/${roomId}/stop`,
        method: 'post'
    })
}

// 直播中列表
export const listLiveRooms = (page = 1, size = 12) => {
    return request({
        url: '/pp/live/rooms',
        method: 'get',
        params: { page, size }
    })
}

// 房间详情
export const getLiveRoom = (roomId: number) => {
    return request({
        url: `/pp/live/rooms/${roomId}`,
        method: 'get'
    })
}

// 进房
export const enterLiveRoom = (roomId: number) => {
    return request({
        url: `/pp/live/rooms/${roomId}/enter`,
        method: 'post'
    })
}

// 离房
export const leaveLiveRoom = (roomId: number) => {
    return request({
        url: `/pp/live/rooms/${roomId}/leave`,
        method: 'post'
    })
}

// 我的直播间（主播台恢复）
export const getMyLiveRoom = () => {
    return request({
        url: '/pp/live/my-room',
        method: 'get'
    })
}

// 推流状态
export const getStreamStatus = (roomId: number) => {
    return request({
        url: `/pp/live/rooms/${roomId}/stream-status`,
        method: 'get'
    })
}

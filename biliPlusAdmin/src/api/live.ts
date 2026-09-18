import { request } from '@/utils/request'

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
    playUrl?: string
    pushUrl?: string
    streamKey?: string
    onlineCount?: number
}

export interface Gift {
    id?: number
    name: string
    iconUrl: string
    price: number
    effectLevel: number
    sortOrder?: number
    status?: number
}

export interface GiftRecord {
    id: number
    liveRoomId: number
    giftId: number
    senderId: number
    hostUserId: number
    unitPrice: number
    count: number
    totalPrice: number
    createTime?: string
}

export interface PageResult {
    total: number
    records: any[]
}

export const getLiveRooms = (params?: { status?: number; page?: number; size?: number }) => {
    return request.get<PageResult>('/admin/live/rooms', params)
}

export const getLiveRoomDetail = (id: number) => {
    return request.get<LiveRoom>(`/admin/live/rooms/${id}`)
}

export const forceStopLive = (id: number) => {
    return request.post<string>(`/admin/live/rooms/${id}/force-stop`)
}

/** 违规封禁：强制下播 + 禁用主播 */
export const banLiveHost = (id: number) => {
    return request.post<string>(`/admin/live/rooms/${id}/ban`)
}

export const unbanUser = (userId: number) => {
    return request.post<string>(`/admin/live/users/${userId}/unban`)
}

export const getGiftRecords = (params?: {
    roomId?: number
    senderId?: number
    hostUserId?: number
    page?: number
    size?: number
}) => {
    return request.get<PageResult>('/admin/live/gift/records', params)
}

export const getGiftList = () => {
    return request.get<Gift[]>('/admin/gifts')
}

export const createGift = (data: Gift) => {
    return request.post<Gift>('/admin/gifts', data)
}

export const updateGift = (id: number, data: Gift) => {
    return request.put<Gift>(`/admin/gifts/${id}`, data)
}

export const deleteGift = (id: number) => {
    return request.delete<string>(`/admin/gifts/${id}`)
}

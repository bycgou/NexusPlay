import { request } from '@/utils/request'

// 视频状态：0待审 1正常 2下架 3审核不通过
export type VideoStatus = 0 | 1 | 2 | 3

export interface AdminVideo {
    id: number
    title: string
    description?: string
    coverUrl?: string
    videoUrl: string
    duration?: string
    userId: number
    categoryId: number
    status: number
    viewCount?: number
    likeCount?: number
    commentCount?: number
    shareCount?: number
    rejectReason?: string
    createTime?: string
    updateTime?: string
    nickname?: string
    avatar?: string
}

export interface PageResult {
    total: number
    records: AdminVideo[]
}

// 按状态分页获取视频
export const getAdminVideoPage = (params: { status?: number; page?: number; pageSize?: number }) => {
    return request.get<PageResult>('/admin/videos/page', params)
}

// 视频详情
export const getAdminVideoDetail = (id: number | string) => {
    return request.get<AdminVideo>(`/admin/videos/${id}`)
}

// 审核通过
export const approveVideo = (id: number | string) => {
    return request.post<string>(`/admin/videos/${id}/approve`)
}

// 审核不通过（需原因）
export const rejectVideo = (id: number | string, reason: string) => {
    return request.post<string>(`/admin/videos/${id}/reject`, { reason })
}

// 下架
export const offlineVideo = (id: number | string) => {
    return request.post<string>(`/admin/videos/${id}/offline`)
}

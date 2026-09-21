import { request } from '@/utils/request'

export interface Report {
    id: number
    reporterId: number
    /** 1视频 2评论 3弹幕 4用户 5直播间 */
    targetType: number
    targetId: number
    /** 1违法 2色情 3辱骂 4广告 5其他 */
    reason: number
    detail?: string
    /** 0待处理 1已处理 2已驳回 */
    status: number
    handlerId?: number
    handleRemark?: string
    createTime?: string
    handleTime?: string
}

export interface PageResult {
    total: number
    records: Report[]
}

export const getReports = (params?: {
    status?: number
    targetType?: number
    page?: number
    size?: number
}) => {
    return request.get<PageResult>('/admin/reports', params)
}

/** 处理举报：status 1成立（联动下架视频/软删评论） 2驳回 */
export const handleReport = (id: number, data: { status: number; remark?: string }) => {
    return request.post<string>(`/admin/reports/${id}/handle`, data)
}

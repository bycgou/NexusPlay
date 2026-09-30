import { request } from '@/utils/request'

export interface AdminMember {
    id: number
    username: string
    nickname?: string
    avatar?: string
    email?: string
    phone?: string
    signature?: string
    /** 0普通 1UP 2管理员 */
    role: number
    /** 0禁用 1正常 */
    status: number
    createTime?: string
    updateTime?: string
    creditScore?: number
    violationCount?: number
    lastViolationTime?: string
    videoCount?: number
    fansCount?: number
    followingCount?: number
    muted?: boolean
    banned?: boolean
}

export interface MemberPenalty {
    id: number
    userId: number
    /** mute|ban */
    action: string
    reason?: string
    startTime?: string
    endTime?: string | null
    adminId?: number
    /** 1生效中 2已到期 3已提前解除 */
    status: number
    createTime?: string
}

export interface MemberPenaltiesResult {
    activeMute: boolean
    activeBan: boolean
    list: { total: number; records: MemberPenalty[] }
}

export interface PageResult<T> {
    total: number
    records: T[]
}

export const getMembers = (params?: {
    keyword?: string
    status?: number
    role?: number
    page?: number
    size?: number
}) => {
    return request.get<PageResult<AdminMember>>('/admin/users', params)
}

export const getMemberDetail = (id: number) => {
    return request.get<AdminMember>(`/admin/users/${id}`)
}

export const getMemberPenalties = (id: number) => {
    return request.get<MemberPenaltiesResult>(`/admin/users/${id}/penalties`)
}

/** days 不传表示永久 */
export const banMember = (id: number, data: { reason?: string; days?: number }) => {
    return request.post<string>(`/admin/users/${id}/ban`, data)
}

export const unbanMember = (id: number) => {
    return request.post<string>(`/admin/users/${id}/unban`)
}

export const muteMember = (id: number, data: { reason?: string; days?: number }) => {
    return request.post<string>(`/admin/users/${id}/mute`, data)
}

export const unmuteMember = (id: number) => {
    return request.post<string>(`/admin/users/${id}/unmute`)
}

export const updateMemberRole = (id: number, role: number) => {
    return request.put<string>(`/admin/users/${id}/role`, { role })
}

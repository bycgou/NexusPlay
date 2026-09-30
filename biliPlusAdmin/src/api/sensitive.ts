import { request } from '@/utils/request'

export interface SensitiveWord {
    id: number
    word: string
    /** 1拦截 2转人工 3仅标记 */
    level: number
    /** 1启用 0停用 */
    status: number
    createTime?: string
}

export interface SensitiveHit {
    id: number
    wordId: number
    word: string
    level: number
    userId: number
    /** video|comment|danmaku|dynamic */
    targetType: string
    targetId: number
    content: string
    /** block|mark */
    action: string
    /** 0待复核 1确认违规 2误伤 */
    reviewStatus: number
    reviewAdminId?: number
    reviewTime?: string
    createTime?: string
}

export interface Penalty {
    id: number
    userId: number
    /** mute|ban */
    action: string
    reason: string
    startTime?: string
    /** null 表示永久 */
    endTime?: string | null
    adminId?: number
    /** 1生效中 2已到期 3已提前解除 */
    status: number
    createTime?: string
}

export interface PageResult<T> {
    total: number
    records: T[]
}

export interface HitSummary {
    total: number
    confirmed: number
    falsePositive: number
    reviewed: number
    /** 尚无复核记录时为 null */
    falsePositiveRate: number | null
    byTargetType: { targetType: string; cnt: number }[]
    topWords: { word: string; cnt: number }[]
}

export const getSensitiveWords = (params?: {
    keyword?: string
    status?: number
    page?: number
    size?: number
}) => {
    return request.get<PageResult<SensitiveWord>>('/admin/sensitive-words', params)
}

export const createSensitiveWord = (data: { word: string; level: number }) => {
    return request.post<SensitiveWord>('/admin/sensitive-words', data)
}

export const updateSensitiveWord = (id: number, data: { level: number; status: number }) => {
    return request.put<SensitiveWord>(`/admin/sensitive-words/${id}`, data)
}

export const deleteSensitiveWord = (id: number) => {
    return request.delete<string>(`/admin/sensitive-words/${id}`)
}

export const getSensitiveHits = (params?: {
    reviewStatus?: number
    action?: string
    page?: number
    size?: number
}) => {
    return request.get<PageResult<SensitiveHit>>('/admin/sensitive-hits', params)
}

export const getHitSummary = () => {
    return request.get<HitSummary>('/admin/sensitive-hits/summary')
}

export const reviewSensitiveHit = (id: number, data: { reviewStatus: 1 | 2 }) => {
    return request.post<string>(`/admin/sensitive-hits/${id}/review`, data)
}

export const getPenalties = (params?: {
    userId?: number
    status?: number
    page?: number
    size?: number
}) => {
    return request.get<PageResult<Penalty>>('/admin/sensitive-hits/penalties', params)
}

/** days 不传表示永久 */
export const createPenalty = (data: {
    userId: number
    action: 'mute' | 'ban'
    reason: string
    days?: number
}) => {
    return request.post<Penalty>('/admin/sensitive-hits/penalties', data)
}

export const releasePenalty = (data: { userId: number; action: 'mute' | 'ban' }) => {
    return request.post<string>('/admin/sensitive-hits/penalties/release', data)
}

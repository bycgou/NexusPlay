import { request } from '@/utils/request'

/** 今日/累计 KPI。缺数时后端补 0，可直接展示 */
export interface StatsOverview {
    totalUsers: number
    todayUsers: number
    totalVideos: number
    todayVideos: number
    pendingVideos: number
    liveRooms: number
    totalGiftAmount: number
    todayGiftAmount: number
    totalDanmaku: number
    todayDanmaku: number
    totalComments: number
    pendingReports: number
    totalEvents: number
}

export interface TrendPoint {
    day: string
    cnt?: number
    amount?: number
}

export interface StatsTrend {
    days: number
    videos: TrendPoint[]
    users: TrendPoint[]
    gifts: TrendPoint[]
}

export const getStatsOverview = () => {
    return request.get<StatsOverview>('/admin/stats/overview')
}

export const getStatsTrend = (days?: number) => {
    return request.get<StatsTrend>('/admin/stats/trend', days ? { days } : undefined)
}

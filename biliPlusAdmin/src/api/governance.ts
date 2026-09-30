import { request } from '@/utils/request'

export interface ReportStat {
    total: number
    pending: number
    overdue: number
    avgHandleMinutes: number
    upheld: number
    rejected: number
}

export interface GovernanceOverview {
    report: ReportStat
    reportDuplicate: { dupTargets: number }
    reasonDistribution: { targetType: number; reason: number; cnt: number }[]
    sensitiveHit: { total: number; confirmed: number; falsePositive: number }
    penaltyRecidivism: { penalizedUsers: number; recidivistUsers: number }
    lowCreditUsers: number
    /** 违规曝光率（默认近 7 天） */
    exposure?: ViolationExposure
}

export interface HandlerWorkload {
    handlerId: number
    cnt: number
    upheld: number
}

export interface ReportSla {
    summary: ReportStat
    handlerWorkload: HandlerWorkload[]
}

export interface TimelinessRow {
    day: string
    cnt: number
    avgHandleMinutes: number
}

export interface Timeliness {
    days: number
    rows: TimelinessRow[]
}

export interface TopViolator {
    userId: number
    score: number
    violationCount: number
    lastViolationTime?: string
    nickname?: string
    username?: string
}

/** 违规曝光率：窗口内 video_view 中目标为违规视频的占比 */
export interface ExposureRow {
    day: string
    totalViews: number
    violationViews: number
}

export interface ViolationExposure {
    days: number
    totalViews: number
    violationViews: number
    /** 分母为 0 时为 null，展示为 — */
    rate: number | null
    violationVideoCount: number
    rows: ExposureRow[]
}

export const getGovernanceOverview = () => {
    return request.get<GovernanceOverview>('/admin/governance/overview')
}

export const getReportSla = () => {
    return request.get<ReportSla>('/admin/governance/report-sla')
}

export const getTimeliness = (days?: number) => {
    return request.get<Timeliness>('/admin/governance/timeliness', days ? { days } : undefined)
}

export const getTopViolators = (limit?: number) => {
    return request.get<{ rows: TopViolator[] }>(
        '/admin/governance/top-violators',
        limit ? { limit } : undefined
    )
}

export const getViolationExposure = (days?: number) => {
    return request.get<ViolationExposure>(
        '/admin/governance/violation-exposure',
        days ? { days } : undefined
    )
}

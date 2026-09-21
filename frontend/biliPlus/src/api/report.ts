import request from '@/utils/request'

export interface ReportTargetTypes {
    /** 1视频 2评论 3弹幕 4用户 5直播间 */
    targetType: number
    targetId: number
    /** 1违法 2色情 3辱骂 4广告 5其他 */
    reason: number
    detail?: string
}

export const REPORT_TARGET = {
    VIDEO: 1,
    COMMENT: 2,
    DANMAKU: 3,
    USER: 4,
    LIVE_ROOM: 5
} as const

export const REPORT_REASONS: { value: number; label: string }[] = [
    { value: 1, label: '违法违规' },
    { value: 2, label: '色情低俗' },
    { value: 3, label: '辱骂攻击' },
    { value: 4, label: '垃圾广告' },
    { value: 5, label: '其他' }
]

// 提交举报；同一目标未结案时后端会拒绝重复提交
export const submitReport = (data: ReportTargetTypes) => {
    return request({
        url: '/pp/reports',
        method: 'post',
        data
    })
}

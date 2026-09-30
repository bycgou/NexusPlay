import { request } from '@/utils/request'

export interface OperationLog {
    id: number
    /** 系统自动动作（如到期解封）为 null */
    adminId?: number
    /** video.approve|user.penalty.ban|report.handle|sensitive.create|... */
    action: string
    targetType?: string
    targetId?: number
    detail?: string
    ip?: string
    createTime?: string
}

export interface PageResult {
    total: number
    records: OperationLog[]
}

export const getOperationLogs = (params?: {
    adminId?: number
    action?: string
    page?: number
    size?: number
}) => {
    return request.get<PageResult>('/admin/operation-logs', params)
}

/** 常见动作，用于下拉筛选 */
export const ACTION_OPTIONS = [
    { value: 'video.approve', label: '审核通过' },
    { value: 'video.reject', label: '审核驳回' },
    { value: 'video.offline', label: '稿件下架' },
    { value: 'report.handle', label: '举报处理' },
    { value: 'user.penalty.ban', label: '封禁' },
    { value: 'user.penalty.mute', label: '禁言' },
    { value: 'user.penalty.auto', label: '自动处罚' },
    { value: 'user.role', label: '角色调整' },
    { value: 'sensitive.create', label: '新增敏感词' },
    { value: 'sensitive.update', label: '修改敏感词' },
    { value: 'sensitive.delete', label: '删除敏感词' },
    { value: 'sensitive.review', label: '命中复核' },
    { value: 'live.forceStop', label: '强制下播' },
    { value: 'live.banHost', label: '封禁主播' },
    { value: 'live.unbanHost', label: '解除主播封禁' },
    { value: 'notification.broadcast', label: '系统通知' }
]

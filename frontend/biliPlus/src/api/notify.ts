import request from '@/utils/request'

export interface NotificationItem {
    id: number
    userId: number
    /** 1评论 2回复 3关注 4审核 5直播开播 6系统 */
    type: number
    title: string
    content?: string
    /** video | comment | live | system */
    bizType?: string
    bizId?: number
    fromUserId?: number
    /** 0未读 1已读 */
    isRead: number
    createTime?: string
}

export interface NotificationPageResult {
    total: number
    records: NotificationItem[]
}

// 通知列表
export const listNotifications = (params: { isRead?: number; type?: number; page?: number; size?: number } = {}) => {
    return request({
        url: '/pp/notifications',
        method: 'get',
        params: { page: 1, size: 20, ...params }
    })
}

// 未读数（未登录返回 0，顶栏可静默轮询）
export const getNotificationUnreadCount = () => {
    return request({
        url: '/pp/notifications/unread-count',
        method: 'get'
    })
}

// 单条已读
export const markNotificationRead = (id: number) => {
    return request({
        url: `/pp/notifications/${id}/read`,
        method: 'post'
    })
}

// 全部已读
export const markAllNotificationsRead = () => {
    return request({
        url: '/pp/notifications/read-all',
        method: 'post'
    })
}

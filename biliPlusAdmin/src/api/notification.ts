import { request } from '@/utils/request'

export interface SystemNotification {
    title: string
    content: string
}

/** 向全站正常用户推送系统通知 */
export const sendSystemNotification = (data: SystemNotification) => {
    return request.post<string>('/admin/notifications', data)
}

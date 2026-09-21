import request from '@/utils/request'

export type UserSettingsPayload = {
  player?: Record<string, any>
  quality?: Record<string, any>
  notify?: Record<string, any>
  privacy?: Record<string, any>
  shortcut?: Record<string, any>
}

/** 获取当前登录用户偏好设置 */
export const getUserSettings = () => {
  return request({
    url: '/pp/people/settings',
    method: 'get'
  })
}

/** 保存/合并偏好设置（仅覆盖传入分类） */
export const saveUserSettings = (data: UserSettingsPayload) => {
  return request({
    url: '/pp/people/settings',
    method: 'put',
    data
  })
}

/** 重置为系统默认 */
export const resetUserSettings = () => {
  return request({
    url: '/pp/people/settings',
    method: 'delete'
  })
}

import { request } from '@/utils/request'

// 登录参数（与后端 AdminUserLoginDTO 一致）
export interface LoginParams {
    account: string
    password: string
}

export interface LoginData {
    id: number
    account: string
    name: string
    token: string
}

export const login = (data: LoginParams) => {
    return request.post<LoginData>('/admin/user/login', data)
}

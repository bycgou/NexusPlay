import axios from 'axios'
// 关键：用 import type 导入类型（仅 TypeScript 识别，不影响运行时）
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'

import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'


interface BaseResponse<T = any> {
    code: number;
    msg: string | null;
    data: T;
}

// 创建Axios实例
const service: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
    headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const authStore = useAuthStore()
        if (authStore.token && config.headers) {
            config.headers.Authorization = `Bearer ${authStore.token}`
        }
        return config
    },
    (error) => {
        ElMessage.error('请求参数错误')
        return Promise.reject(error)
    }
)

// 响应拦截器（把 AxiosResponse 拆成业务层 BaseResponse）
service.interceptors.response.use(
    (response: AxiosResponse): any => {
        const res = response.data as BaseResponse
        if (res.code !== 1) {
            ElMessage.error(res.msg || '接口请求失败')
            return Promise.reject(res) // 失败时reject，进入catch
        }
        return res // 成功时返回完整响应（包含code、msg、data）
    },
    (error) => {
        // 错误处理逻辑不变
        if (error.response?.status === 401) {
            const authStore = useAuthStore()
            authStore.logout()
            router.push('/login')
            ElMessage.warning('登录已过期，请重新登录')
        } else {
            ElMessage.error('网络错误，请稍后重试')
        }
        return Promise.reject(error)
    }
)

// 封装请求方法（带类型约束）
export const request = {
    get: <T = any>(url: string, params?: any) => {
        return service.get<T>(url, { params })
    },
    post: <T = any>(url: string, data?: any) => {
        return service.post<T>(url, data)
    },
    put: <T = any>(url: string, data?: any) => {
        return service.put<T>(url, data)
    },
    delete: <T = any>(url: string, params?: any) => {
        return service.delete<T>(url, { params })
    }
}

export default service
// src/utils/request.ts
import axios from "axios";
import { ElMessage } from 'element-plus';
import { useUserStore } from "@/store/user.ts";
import router from '@/router'

// 创建Axios实例
const service = axios.create({
    baseURL: import.meta.env.VITE_BASE_API,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
});

// 请求拦截器：自动携带Token
service.interceptors.request.use(
    (config) => {

        console.log('Token from localStorage:', localStorage.getItem('token'));

        const token = localStorage.getItem('token');
        // 严格匹配store中的token字段，使用Bearer规范格式
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        ElMessage.error('请求发送失败：' + error.message);
        return Promise.reject(error);
    }
);

// 响应拦截器：统一处理错误和Token过期
service.interceptors.response.use(
    (response) => {
        // 直接返回响应数据（匹配你的业务逻辑）
        return response.data;
    },
    (error) => {
        const status = error.response?.status;

        // 针对401（Token无效/过期）的特殊处理
        if (status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            ElMessage.error('登录状态已失效，请重新登录');
            router.push('/login').catch(err => console.warn('路由跳转失败：', err));

        } else {
            // 其他错误提示（优先使用后端返回的msg）
            const errMsg = error.response?.data?.msg || error.message || '请求失败';
            ElMessage.error(errMsg);
        }
        return Promise.reject(error);
    }
);

export default service;
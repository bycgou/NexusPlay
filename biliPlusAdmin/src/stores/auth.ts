import { defineStore } from 'pinia'

// 定义用户信息类型
interface UserInfo {
    id: string
    name: string
    avatar?: string
}

// 定义Store类型
interface AuthState {
    token: string | null
    userInfo: UserInfo | null
}

const TOKEN_KEY = 'admin_token'
const USER_KEY = 'admin_userInfo'

export const useAuthStore = defineStore('auth', {
    state: (): AuthState => ({
        token: localStorage.getItem(TOKEN_KEY) || localStorage.getItem('token') || null,
        userInfo: (() => {
            try {
                const raw = localStorage.getItem(USER_KEY)
                return raw ? JSON.parse(raw) : null
            } catch {
                return null
            }
        })()
    }),
    actions: {
        login(token: string, userInfo: UserInfo) {
            this.token = token
            this.userInfo = userInfo
            localStorage.setItem(TOKEN_KEY, token)
            // 兼容路由守卫读取的 token 键
            localStorage.setItem('token', token)
            localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
        },
        logout() {
            this.token = null
            this.userInfo = null
            localStorage.removeItem(TOKEN_KEY)
            localStorage.removeItem('token')
            localStorage.removeItem(USER_KEY)
        }
    },
    getters: {
        isLogin: (state) => !!state.token
    }
})

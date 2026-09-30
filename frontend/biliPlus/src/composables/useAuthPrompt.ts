import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 全局登录/注册弹窗状态。
 * 任意页面通过 openLogin/openRegister/requireLogin 唤起，无需跳转 /login 独立页。
 */
export const authPrompt = reactive({
    visible: false,
    mode: 'login' as 'login' | 'register',

    openLogin() {
        this.mode = 'login'
        this.visible = true
    },

    openRegister() {
        this.mode = 'register'
        this.visible = true
    },

    close() {
        this.visible = false
    },

    /** 写操作前调用：未登录则弹窗并返回 false */
    requireLogin(tip = '请先登录后再操作') {
        if (localStorage.getItem('token')) {
            return true
        }
        ElMessage.warning(tip)
        this.openLogin()
        return false
    }
})

export function useAuthPrompt() {
    return authPrompt
}

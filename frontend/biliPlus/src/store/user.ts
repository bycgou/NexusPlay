// src/store/user.ts（最终优化版）
import { defineStore } from "pinia";
import axios from "axios";

export const useUserStore = defineStore("user", {
    state: () => ({
        // 加固：try-catch 避免 JSON.parse 报错
        userInfo: (() => {
            const stored = localStorage.getItem("userInfo");
            if (!stored) return {};
            try {
                return JSON.parse(stored);
            } catch (err) {
                console.warn("本地存储 userInfo 格式错误，已重置", err);
                localStorage.removeItem("userInfo");
                return {};
            }
        })(),
        token: localStorage.getItem("token") || ""
    }),
    getters: {
        isLogin: (state) => !!state.token,
        userRole: (state) => state.userInfo.role || "guest"
    },
    actions: {
        login(loginRes) {
            // 解构后端返回的 data（确保后端返回格式匹配）
            const data = loginRes||{};
            this.token = data.token || '';
            this.userInfo = {
                username: data.username || '', // 强制存储，兜底空字符串
                nickname: data.nickname || '', // 强制存储，兜底空字符串
                id: data.id || '',
                email: data.email || '',
                role: data.role || 0,
                avatar: data.avatar || ''
            };
            // 同步本地存储（确保 userInfo 是可序列化对象）
            localStorage.setItem("token", this.token);
            localStorage.setItem("userInfo", JSON.stringify(this.userInfo));
            // 可选：打印调试，确认存储的数据
            console.log("Pinia 存储的 userInfo：", this.userInfo);
        },
        logout() {
            this.token = "";
            this.userInfo = {};
            localStorage.removeItem("token");
            localStorage.removeItem("userInfo");
        }
    }
});
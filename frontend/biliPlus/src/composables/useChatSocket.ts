// src/composables/useChatSocket.js
import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

// 全局 socket 实例（确保整个应用只有一个连接）
let globalSocket = null

/**
 * 管理全局 WebSocket 连接
 * @returns {{socket: Ref<WebSocket | null>, connect: (function(string): void)}}
 */
export function useChatSocket() {
    const socket = ref(null)

    const connect = (token) => {
        if (!token) {
            ElMessage.error('未提供有效 token')
            return
        }

        // 如果已有有效连接，直接复用
        if (globalSocket && globalSocket.readyState === WebSocket.OPEN) {
            socket.value = globalSocket
            return
        }

        // 关闭旧连接（如果存在）
        if (globalSocket) {
            globalSocket.close()
        }

        const wsUrl = `ws://localhost:8081/ws/chat?token=${encodeURIComponent(token)}`
        globalSocket = new WebSocket(wsUrl)
        socket.value = globalSocket

        globalSocket.onopen = () => {
            console.log('✅ WebSocket 连接成功（已携带 token 认证）')
        }

        globalSocket.onclose = () => {
            console.log('🔌 WebSocket 已断开')
            globalSocket = null
        }

        globalSocket.onerror = (error) => {
            console.error('❌ WebSocket 错误:', error)
            ElMessage.error('聊天连接异常，请刷新页面')
        }
    }

    // 组件卸载时不关闭全局连接（可选：如果你希望页面离开就断开，则移除此注释）
    // onUnmounted(() => {
    //   if (globalSocket && socket.value === globalSocket) {
    //     globalSocket.close()
    //     globalSocket = null
    //   }
    // })

    return {
        socket,
        connect
    }
}
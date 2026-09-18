// src/composables/useLiveSocket.ts
import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

export interface LiveSocketHandlers {
    onChat?: (msg: any) => void
    onOnline?: (msg: any) => void
    onGift?: (msg: any) => void
    onPkStart?: (msg: any) => void
    onPkScore?: (msg: any) => void
    onPkEnd?: (msg: any) => void
    onPkInvite?: (msg: any) => void
    onPkCancel?: (msg: any) => void
    onMicApply?: (msg: any) => void
    onMicReady?: (msg: any) => void
    onMicEnd?: (msg: any) => void
    onMicResult?: (msg: any) => void
    onRoomClose?: (msg: any) => void
    onOpen?: () => void
    onClose?: () => void
}

export function useLiveSocket() {
    const socket = ref<WebSocket | null>(null)
    const connected = ref(false)
    let handlers: LiveSocketHandlers = {}
    let joinedRoomId: number | null = null
    let manualClose = false

    const dispatch = (data: any) => {
        const t = data?.type
        switch (t) {
            case 'chat': handlers.onChat?.(data); break
            case 'online': handlers.onOnline?.(data); break
            case 'gift': handlers.onGift?.(data); break
            case 'pk_start': handlers.onPkStart?.(data); break
            case 'pk_score': handlers.onPkScore?.(data); break
            case 'pk_end': handlers.onPkEnd?.(data); break
            case 'pk_invite': handlers.onPkInvite?.(data); break
            case 'pk_cancel': handlers.onPkCancel?.(data); break
            case 'mic_apply': handlers.onMicApply?.(data); break
            case 'mic_ready': handlers.onMicReady?.(data); break
            case 'mic_end': handlers.onMicEnd?.(data); break
            case 'mic_result': handlers.onMicResult?.(data); break
            case 'room_close': handlers.onRoomClose?.(data); break
            default: break
        }
    }

    const connect = (token: string, roomId: number, h: LiveSocketHandlers) => {
        handlers = h
        joinedRoomId = roomId
        manualClose = false
        if (!token) {
            ElMessage.warning('请先登录')
            return
        }
        if (socket.value && socket.value.readyState === WebSocket.OPEN) {
            join(roomId)
            return
        }
        const wsUrl = `ws://localhost:8081/ws/live?token=${encodeURIComponent(token)}`
        const ws = new WebSocket(wsUrl)
        socket.value = ws

        ws.onopen = () => {
            connected.value = true
            handlers.onOpen?.()
            join(roomId)
        }
        ws.onmessage = (ev) => {
            try {
                dispatch(JSON.parse(ev.data))
            } catch (e) {
                console.warn('live ws parse error', e)
            }
        }
        ws.onclose = () => {
            connected.value = false
            socket.value = null
            handlers.onClose?.()
        }
        ws.onerror = () => {
            console.error('live ws error')
        }
    }

    const join = (roomId: number) => {
        send({ type: 'join', roomId })
    }

    const send = (payload: object) => {
        if (socket.value && socket.value.readyState === WebSocket.OPEN) {
            socket.value.send(JSON.stringify(payload))
        }
    }

    const chat = (roomId: number, content: string) => {
        send({ type: 'chat', roomId, content })
    }

    const micApply = (roomId: number) => {
        send({ type: 'mic_apply', roomId })
    }

    const micAccept = (applyUserId: number, agree = true) => {
        send({ type: 'mic_accept', applyUserId, agree })
    }

    const micLeave = (roomId: number) => {
        send({ type: 'mic_leave', roomId })
    }

    const disconnect = () => {
        manualClose = true
        if (joinedRoomId != null) {
            send({ type: 'leave', roomId: joinedRoomId })
        }
        if (socket.value) {
            socket.value.close()
            socket.value = null
        }
        connected.value = false
    }

    onUnmounted(() => {
        if (!manualClose) {
            disconnect()
        }
    })

    return {
        socket,
        connected,
        connect,
        disconnect,
        send,
        chat,
        micApply,
        micAccept,
        micLeave
    }
}

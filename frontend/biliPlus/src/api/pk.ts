import request from '@/utils/request'

export interface LivePk {
    id: number
    roomAId: number
    roomBId: number
    hostAId: number
    hostBId: number
    status: number
    scoreA: number
    scoreB: number
    durationSec?: number
    startTime?: string
    endTime?: string
}

// 发起 PK
export const invitePk = (opponentRoomId: number) => {
    return request({
        url: '/pp/live/pk/invite',
        method: 'post',
        data: { opponentRoomId }
    })
}

// 响应 PK
export const respondPk = (pkId: number, agree: boolean) => {
    return request({
        url: `/pp/live/pk/${pkId}/response`,
        method: 'post',
        data: { agree }
    })
}

// 当前 PK
export const getActivePk = (roomId: number) => {
    return request({
        url: '/pp/live/pk/active',
        method: 'get',
        params: { roomId }
    })
}

// 结束 PK
export const endPk = (pkId: number) => {
    return request({
        url: `/pp/live/pk/${pkId}/end`,
        method: 'post'
    })
}

// 连麦申请记录（主播侧 pending）
export const listMicPending = (roomId: number) => {
    return request({
        url: `/pp/live/rooms/${roomId}/mic/history`,
        method: 'get'
    })
}

// 连麦离开
export const leaveMic = (roomId: number) => {
    return request({
        url: `/pp/live/rooms/${roomId}/mic/leave`,
        method: 'post'
    })
}

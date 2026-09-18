import request from '@/utils/request'

export interface Gift {
    id: number
    name: string
    iconUrl: string
    price: number
    effectLevel: number
    sortOrder?: number
    status?: number
}

export interface Wallet {
    userId: number
    balance: number
}

export interface GiftSendResult {
    recordId: number
    balance: number
    combo: number
    effectLevel: number
}

// 礼物目录
export const listGifts = () => {
    return request({
        url: '/pp/live/gifts',
        method: 'get'
    })
}

// 查余额
export const getWallet = () => {
    return request({
        url: '/pp/live/wallet',
        method: 'get'
    })
}

// 测试充值
export const rechargeWallet = (amount: number) => {
    return request({
        url: '/pp/live/wallet/recharge',
        method: 'post',
        data: { amount }
    })
}

// 送礼
export const sendGift = (roomId: number, giftId: number, count: number) => {
    return request({
        url: `/pp/live/rooms/${roomId}/gifts`,
        method: 'post',
        data: { giftId, count }
    })
}

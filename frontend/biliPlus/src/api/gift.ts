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

// 测试充值（兼容旧接口，内部同样走「创建订单 + 模拟支付」）
export const rechargeWallet = (amount: number) => {
    return request({
        url: '/pp/live/wallet/recharge',
        method: 'post',
        data: { amount }
    })
}

export interface RechargeOrder {
    orderNo: string
    amount: number
    payAmount: number
    status: number
    paidTime?: string
    createTime?: string
}

export interface WalletTransaction {
    id: number
    userId: number
    type: number
    amount: number
    balanceAfter: number
    bizType: string
    bizId?: number
    remark?: string
    createTime?: string
}

// 创建充值订单（待支付）
export const createRechargeOrder = (amount: number) => {
    return request({
        url: '/pp/live/wallet/recharge/orders',
        method: 'post',
        data: { amount }
    })
}

// 模拟支付充值订单
export const payRechargeOrder = (orderNo: string) => {
    return request({
        url: `/pp/live/wallet/recharge/orders/${orderNo}/pay`,
        method: 'post'
    })
}

// 我的账变流水
export const listWalletTransactions = (params: { type?: number; page?: number; size?: number } = {}) => {
    return request({
        url: '/pp/live/wallet/transactions',
        method: 'get',
        params: { page: 1, size: 20, ...params }
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

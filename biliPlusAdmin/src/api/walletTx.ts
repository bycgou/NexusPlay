import { request } from '@/utils/request'

export interface WalletTransaction {
    id: number
    userId: number
    /** 1充值 2送礼支出 3主播收入 4系统调整 */
    type: number
    /** 变动金额，正为入账，负为出账（单位：硬币） */
    amount: number
    balanceAfter: number
    /** recharge|gift|host_income|adjust */
    bizType: string
    bizId?: number
    remark?: string
    createTime?: string
}

export interface PageResult {
    total: number
    records: WalletTransaction[]
}

export const getWalletTransactions = (params?: {
    userId?: number
    type?: number
    bizType?: string
    /** 支持 YYYY-MM-DD，后端按天自动补全时分秒 */
    from?: string
    to?: string
    page?: number
    size?: number
}) => {
    return request.get<PageResult>('/admin/live/wallet/transactions', params)
}

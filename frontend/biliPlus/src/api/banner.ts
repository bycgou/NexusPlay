import request from '@/utils/request'

// 首页轮播图（仅上线）
export const getBanners = () => {
    return request({
        url: '/pp/banners',
        method: 'get'
    })
}

import request from '@/utils/request'

// 获取我的投稿列表（含审核状态与不通过原因）
export const getMyVideos = () => {
    return request({
        url: '/pp/people/my/videos',
        method: 'get'
    })
}

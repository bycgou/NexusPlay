import request from '@/utils/request'

// 获取我的投稿列表（含审核状态、不通过原因与标签）
export const getMyVideos = () => {
    return request({
        url: '/pp/people/my/videos',
        method: 'get'
    })
}

// 编辑自有稿件：标题/简介/分类/封面/标签（tags 为逗号分隔字符串）
export const updateMyVideo = (id, data) => {
    return request({
        url: `/pp/people/my/videos/${id}`,
        method: 'put',
        data
    })
}

// 删除（软删）自有稿件
export const deleteMyVideo = (id) => {
    return request({
        url: `/pp/people/my/videos/${id}`,
        method: 'delete'
    })
}

// 驳回/下架稿件修改后重新提交审核
export const resubmitMyVideo = (id) => {
    return request({
        url: `/pp/people/my/videos/${id}/resubmit`,
        method: 'post'
    })
}

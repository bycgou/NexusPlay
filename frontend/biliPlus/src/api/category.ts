import request from '@/utils/request'

// 获取公开分类列表；type=1 视频，type=2 直播
export const getCategories = (type?: number) => {
    return request({
        url: '/pp/categories',
        method: 'get',
        params: type ? { type } : undefined
    })
}

// 管理端：新增分类
export const createCategory = (data) => {
    return request({
        url: '/admin/category',
        method: 'post',
        data
    })
}

// 管理端：修改分类
export const updateCategory = (id, data) => {
    return request({
        url: `/admin/category/${id}`,
        method: 'put',
        data
    })
}

// 管理端：删除分类
export const deleteCategory = (id) => {
    return request({
        url: `/admin/category/${id}`,
        method: 'delete'
    })
}

import request from '@/utils/request'

// 获取公开分类列表
export const getCategories = () => {
    return request({
        url: '/pp/categories',
        method: 'get'
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

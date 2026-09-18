import { request } from '@/utils/request'

export interface Category {
    id?: number
    name: string
    parentId?: number
    sortOrder?: number
    icon?: string
    /** 1视频分区 2直播分区 */
    type?: number
    createTime?: string
    updateTime?: string
}

export const getCategoryList = (type?: number) => {
    return request.get<Category[]>('/admin/category/list', type ? { type } : undefined)
}

export const createCategory = (data: Category) => {
    return request.post<Category>('/admin/category', data)
}

export const updateCategory = (id: number, data: Category) => {
    return request.put<Category>(`/admin/category/${id}`, data)
}

export const deleteCategory = (id: number) => {
    return request.delete<string>(`/admin/category/${id}`)
}

import { request } from '@/utils/request'

export interface Banner {
    id?: number
    title: string
    description?: string
    imageUrl: string
    /** 1-视频 2-外链 3-不跳转 */
    linkType: number
    videoId?: number | null
    linkUrl?: string | null
    sortOrder?: number
    /** 0下线 1上线 */
    status?: number
    createTime?: string
    updateTime?: string
}

export const getBannerList = () => {
    return request.get<Banner[]>('/admin/banners/list')
}

export const createBanner = (data: Banner) => {
    return request.post<Banner>('/admin/banners', data)
}

export const updateBanner = (id: number, data: Banner) => {
    return request.put<Banner>(`/admin/banners/${id}`, data)
}

export const deleteBanner = (id: number) => {
    return request.delete<string>(`/admin/banners/${id}`)
}

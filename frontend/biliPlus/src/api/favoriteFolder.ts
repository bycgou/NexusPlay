import request from '@/utils/request'

export interface FavoriteFolder {
    id: number
    name: string
    isDefault: number
    isPrivate: number
    createTime?: string
    videoCount?: number
}

// 我的收藏夹列表（后端会在缺失时自动补建默认夹）
export const listFavoriteFolders = () => {
    return request({
        url: '/pp/favorite-folders',
        method: 'get'
    })
}

export const createFavoriteFolder = (data: { name: string; isPrivate?: boolean }) => {
    return request({
        url: '/pp/favorite-folders',
        method: 'post',
        data
    })
}

export const updateFavoriteFolder = (id: number, data: { name?: string; isPrivate?: boolean }) => {
    return request({
        url: `/pp/favorite-folders/${id}`,
        method: 'put',
        data
    })
}

export const deleteFavoriteFolder = (id: number) => {
    return request({
        url: `/pp/favorite-folders/${id}`,
        method: 'delete'
    })
}

// 夹内视频分页
export const listFolderVideos = (id: number, params: { page?: number; size?: number } = {}) => {
    return request({
        url: `/pp/favorite-folders/${id}/videos`,
        method: 'get',
        params: { page: 1, size: 20, ...params }
    })
}

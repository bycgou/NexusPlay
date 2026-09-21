import request from '@/utils/request'

/** 番剧列表；status: 0未播出 1连载中 2已完结 */
export const getAnimeList = (params: {
  page?: number
  pageSize?: number
  status?: number
  keyword?: string
} = {}) => {
  return request({
    url: '/pp/anime',
    method: 'get',
    params
  })
}

export const getAnimeDetail = (id: number) => {
  return request({
    url: `/pp/anime/${id}`,
    method: 'get'
  })
}

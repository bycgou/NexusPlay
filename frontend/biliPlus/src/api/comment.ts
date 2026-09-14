// api/comment.js
import request from '@/utils/request'

export const postComment = (videoId, content, parentId = 0) => {
    return request({
        url: `/pp/comments`,
        method: 'POST',
        data: { videoId, content, parentId }
    })
}

export const getComments = (videoId) => {
    return request({
        url: `/pp/comments?videoId=${videoId}`,
        method: 'GET'
    })
}

// 评论点赞/取消
export const toggleCommentLike = (commentId) => {
    return request({
        url: `/pp/comments/${commentId}/like`,
        method: 'POST'
    })
}

// 删除评论
export const deleteComment = (videoId, commentId) => {
    return request({
        url: `/pp/comments`,
        method: 'DELETE',
        data: { videoId, commentId }
    })
}

export {request}
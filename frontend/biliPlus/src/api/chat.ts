import request from "@/utils/request.ts";

// 创建私聊会话
export const createConversation = (params)=>{
    return request({
        url: `/pp/chat/conversations/private`,
        method: 'POST',
        data: params
    })
}

// 获取会话列表
export const getConversationList = ()=>{
    return request({
        url: `/pp/chat/conversations`,
        method: 'GET'
    })
}

// 获取会话消息
export const getConversationMessage = (convId: number | string)=>{
    return request({
        url: `/pp/chat/conversations/${convId}/messages`,
        method: 'GET'
    })
}

// 未读私信总数（顶栏红点）
export const getUnreadTotal = () => {
    return request({
        url: '/pp/chat/unread-total',
        method: 'GET'
    })
}

// 标记会话已读
export const markConversationAsRead = (conversationId: number | string) => {
    return request({
        url: `/pp/chat/conversations/${conversationId}/read`,
        method: 'POST'
    })
}

// 根据视频作者 name（即 nickname）查询作者信息
export const getAuthorIdByName = (name: string) => {
    return request({
        url: '/pp/people/username',
        method: 'GET',
        params: {
            nickname: name
        }
    })
}

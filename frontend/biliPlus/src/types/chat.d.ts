// src/types/chat.d.ts 定义类型,全局共享

export interface ChatMessage {
    id: number;
    senderId: number;
    conversationId: number;
    content: string;
    msgType: number; // 1: 文本
    createdAt: string; // ISO 8601
}

export interface PageResult<T> {
    list: T[];
    total: number;
    pageNum: number;
    pageSize: number;
    totalPages: number;
}

export interface MarkReadRequest {
    conversationId: number;
    lastReadMsgId: number;
}
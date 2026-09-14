<!-- src/views/chat/components/ChatSidebar.vue -->
<template>
  <div class="chat-sidebar">
    <div class="sidebar-header">
      <h3>消息</h3>
      <span class="count" v-if="conversations.length">{{ conversations.length }}</span>
    </div>

    <div class="list-wrap">
      <el-empty v-if="conversations.length === 0" description="暂无会话" :image-size="64" />

      <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          :class="{ active: conv.id === selectedId }"
          @click="selectConversation(conv)"
      >
        <el-avatar :src="getAvatar(conv)" :size="42" class="avatar" />
        <div class="info">
          <div class="row-top">
            <span class="name">{{ getDisplayName(conv) }}</span>
            <span class="time">{{ formatDate(conv.lastMessageAt || conv.lastMessage?.createdAt) }}</span>
          </div>
          <div class="row-bottom">
            <span class="preview">{{ getLastMessagePreview(conv) }}</span>
            <el-badge
                v-if="getUnreadCount(conv) > 0"
                :value="getUnreadCount(conv) > 99 ? '99+' : getUnreadCount(conv)"
                type="danger"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import defaultAvatar from '@/assets/default-avatar.jpg'
import { useUserStore } from '@/store/user.js'

const userStore = useUserStore()

const props = defineProps({
  conversations: { type: Array, required: true, default: () => [] },
  selectedId: { type: [Number, String, null], default: null },
})

const emit = defineEmits(['select-conversation'])

const currentUserId = userStore.userInfo?.id

const getAvatar = (conv) => {
  if (conv.otherUser) return conv.otherUser.avatar || defaultAvatar
  if (conv.type === 1 && conv.members) {
    const other = conv.members.find((m) => m.userId !== currentUserId)
    return other?.avatar || defaultAvatar
  }
  return conv.avatar || defaultAvatar
}

const getDisplayName = (conv) => {
  if (conv.otherUser) return conv.otherUser.name || conv.otherUser.username || '未知用户'
  if (conv.type === 1 && conv.members) {
    const other = conv.members.find((m) => m.userId !== currentUserId)
    return other?.username || other?.name || '未知用户'
  }
  return conv.name || '群聊'
}

const getUnreadCount = (conv) => {
  if (conv.otherUser) return conv.unreadCount || 0
  const member = conv.members?.find((m) => m.userId === currentUserId)
  return member?.unreadCount || 0
}

const getLastMessagePreview = (conv) => {
  const msg = conv.lastMessage
  if (!msg) return '暂无消息'
  if (msg.msgType === 2) return '[图片]'
  try {
    const parsed = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content
    if (parsed?.type === 'image') return '[图片]'
    return parsed?.content || parsed || msg.content
  } catch (e) {
    return msg.content || '[消息]'
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  return date.toLocaleDateString()
}

const selectConversation = (conv) => emit('select-conversation', conv)
</script>

<style scoped>
.chat-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 18px;
  border-bottom: 1px solid #eef0f4;
  flex-shrink: 0;
  background: #fff;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #18191c;
}

.count {
  font-size: 12px;
  color: #94a3b8;
  background: #f1f2f3;
  border-radius: 10px;
  padding: 1px 8px;
}

.list-wrap {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 6px 0;
}

.conversation-item {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s ease;
  border-left: 3px solid transparent;
}

.conversation-item:hover {
  background: #f5f6f8;
}

.conversation-item.active {
  background: #ece9fe;
  border-left-color: #6c5ce7;
}

.avatar {
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.row-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.name {
  font-size: 14px;
  font-weight: 600;
  color: #18191c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time {
  font-size: 11px;
  color: #94a3b8;
  flex-shrink: 0;
}

.row-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 4px;
}

.preview {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}
</style>

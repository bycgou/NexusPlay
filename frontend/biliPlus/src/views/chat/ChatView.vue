<!-- src/views/chat/ChatView.vue -->
<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import ChatSidebar from '@/views/chat/components/ChatSidebar.vue'
import ChatMessages from '@/views/chat/components/ChatMessages.vue'
import ChatInput from '@/views/chat/components/ChatInput.vue'
import defaultAvatar from '@/assets/default-avatar.jpg'
import VideoCallModal from '@/views/chat/components/VideoCallModal.vue'

import {
  createConversation,
  getConversationList,
  getConversationMessage,
  markConversationAsRead
} from '@/api/chat.js'
import { useUserStore } from '@/store/user.js'
import { useChatSocket } from '@/composables/useChatSocket.js'

// ====== 初始化 ======
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const CURRENT_USER_ID = userStore.userInfo?.id || null

// 移动端布局标记（模板中使用）
const isMobile = computed(() => window.innerWidth < 768)

// WebSocket（单例）
const { socket: chatSocket, connect: connectSocket } = useChatSocket()
provide('chatSocket', chatSocket) // 👈 提供给 VideoCallModal

// Refs
const videoCallModalRef = ref(null)
const messagesRef = ref(null)

// Data
const currentConversation = ref(null)
const messages = ref([])
const conversationList = ref([])
const isVideoCalling = ref(false)

// Computed
const displayedMessages = computed(() =>
    messages.value.map(msg => ({
      ...msg,
      isSelf: msg.senderId === CURRENT_USER_ID
    }))
)

const currentContact = computed(() => {
  const otherUser = currentConversation.value?.otherUser
  if (!otherUser) {
    return { id: null, name: '未知联系人', avatar: defaultAvatar }
  }
  return {
    id: otherUser.id,
    name: otherUser.name || '联系人',
    avatar: otherUser.avatar || defaultAvatar
  }
})

// ====== 视频通话控制 ======
const openVideoCall = () => {
  if (!currentConversation.value?.id || !currentContact.value?.id) {
    ElMessage.warning('请选择一个有效的聊天对象再发起视频通话')
    return
  }

  isVideoCalling.value = true
  nextTick(() => {
    // 主叫方：主动发起
    videoCallModalRef.value?.startOutgoingCall()
  })
}

const closeVideoCall = () => {
  isVideoCalling.value = false
}

// ====== 消息 & API ======
const fetchConversations = async () => {
  try {
    const res = await getConversationList()
    console.log('获取会话列表返回结果:', res)
    if (res.code === 1) {
      conversationList.value = res.data || []
      console.log('初始化后的会话列表:', conversationList.value)
    }
  } catch (err) {
    console.error('获取会话失败:', err)
    ElMessage.error('加载会话失败')
  }
}

const loadMessages = async (conversationId) => {
  try {
    const res = await getConversationMessage(conversationId)
    if (res.code === 1) {
      messages.value = [...(res.data || [])].reverse()
      nextTick(scrollToBottom)
    }
  } catch (err) {
    console.error('加载消息失败:', err)
    ElMessage.error('加载历史消息失败')
  }
}

const scrollToBottom = () => {
  const el = messagesRef.value
  if (el) el.scrollTop = el.scrollHeight
}

// ====== 发送普通消息 ======
const sendToSocket = (content, msgType = '1') => {
  const socket = chatSocket.value
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    ElMessage.warning('连接异常，请稍后再试')
    return
  }

  const message = {
    conversationId: String(currentConversation.value.id),
    content: content,
    msgType: String(msgType)
  }

  socket.send(JSON.stringify(message))
}

// ====== WebSocket 消息处理（核心！）======
const handleSocketMessage = (event) => {
  try {
    const data = JSON.parse(event.data)
    console.log('📩 收到 WebSocket 消息:', data)

    // 🔑 WebRTC 信令处理（msgType = 0）
    if (data.msgType === 0 && data.extraData) {
      const signal = JSON.parse(data.extraData)
      console.log('📡 收到 WebRTC 信令:', signal)

      if (signal.type === 'offer') {
        // 被叫方：自动弹出视频窗口并初始化
        if (!isVideoCalling.value) {
          const targetConv = conversationList.value.find(
              c => c.id == data.conversationId
          )
          if (targetConv) {
            currentConversation.value = targetConv
            messages.value = []
          } else {
            // 如果会话不存在，可选：创建或忽略
            console.warn('收到 offer，但会话不存在:', data.conversationId)
          }
          isVideoCalling.value = true

          // 延迟确保 modal 已挂载
          setTimeout(() => {
            videoCallModalRef.value?.startIncomingCall(signal)
          }, 300)
        }
      } else {
        // answer / candidate：直接转发
        setTimeout(() => {
          videoCallModalRef.value?.handleIncomingSignal(signal)
        }, 50)
      }
      return
    }

    // 普通消息
    if (data.conversationId != null && data.content !== undefined) {
      const msg = {
        id: data.id,
        senderId: data.senderId,
        conversationId: data.conversationId,
        content: data.content,
        msgType: parseInt(data.msgType, 10) || 1,
        createdAt: data.createdAt || new Date().toISOString(),
        isSelf: data.senderId === CURRENT_USER_ID
      }

      if (currentConversation.value?.id == data.conversationId) {
        messages.value.push(msg)
        nextTick(scrollToBottom)
      }
    }
  } catch (e) {
    console.warn('❌ WebSocket 消息解析失败:', e)
  }
}

// ====== 用户交互 ======
const handleSelectConversation = async (conv) => {
  currentConversation.value = conv
  messages.value = []
  await loadMessages(conv.id)
  // 标记已读，清空该会话未读（顶栏红点依赖 unread_count）
  try {
    await markConversationAsRead(conv.id)
    if (conv.unreadCount != null) conv.unreadCount = 0
    if (conv.members) {
      const me = conv.members.find((m) => m.userId === CURRENT_USER_ID)
      if (me) me.unreadCount = 0
    }
  } catch (e) {
    console.warn('标记已读失败', e)
  }
}

const handleSendMessage = (content) => {
  if (!currentConversation.value || !content.trim()) return

  const tempMsg = {
    id: Date.now(),
    senderId: CURRENT_USER_ID,
    conversationId: currentConversation.value.id,
    content,
    msgType: 1,
    createdAt: new Date().toISOString(),
    isSelf: true
  }

  messages.value.push(tempMsg)
  nextTick(scrollToBottom)
  sendToSocket(content, '1')
}

const handleInsertImage = (url) => {
  if (!currentConversation.value || !url) return
  const tempMsg = {
    id: Date.now(),
    senderId: CURRENT_USER_ID,
    conversationId: currentConversation.value.id,
    content: url,
    msgType: 2,
    createdAt: new Date().toISOString(),
    isSelf: true
  }
  messages.value.push(tempMsg)
  nextTick(scrollToBottom)
  sendToSocket(url, '2')
}

// ====== 处理路由跳转私信 ======
const handlePrivateChatFromQuery = async () => {
  const { toUserId, toUserName, toUserAvatar } = route.query
  console.log('路由参数:', { toUserId, toUserName, toUserAvatar })
  if (!toUserId) return

  const existing = conversationList.value.find(
      conv => conv.otherUser?.id === Number(toUserId)
  )
  console.log('现有会话:', existing)

  if (existing) {
    // 更新现有会话的用户信息
    if (toUserName || toUserAvatar) {
      existing.otherUser = {
        ...existing.otherUser,
        name: toUserName || existing.otherUser?.name || '未知用户',
        avatar: toUserAvatar || existing.otherUser?.avatar || defaultAvatar
      }
    }
    console.log('更新后的现有会话:', existing)
    currentConversation.value = existing
    await loadMessages(existing.id)
    router.replace({ query: {} })
    return
  }

  try {
    const res = await createConversation({ targetUserId: Number(toUserId) })
    console.log('创建会话返回结果:', res)
    if (res.code === 1) {
      const newConv = res.data
      // 确保otherUser对象存在
      if (!newConv.otherUser) {
        newConv.otherUser = {}
      }
      // 合并路由参数中的用户信息到会话对象
      newConv.otherUser = {
        ...newConv.otherUser,
        id: Number(toUserId),
        name: toUserName || newConv.otherUser?.name || '未知用户',
        avatar: toUserAvatar || newConv.otherUser?.avatar || defaultAvatar
      }
      console.log('新创建的会话:', newConv)
      conversationList.value.unshift(newConv)
      console.log('更新后的会话列表:', conversationList.value)
      currentConversation.value = newConv
      messages.value = []
      router.replace({ query: {} })
    }
  } catch (err) {
    console.error('创建私信失败:', err)
    ElMessage.error('创建私信失败')
  }
}

// ====== 生命周期 ======
onMounted(async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.error('用户信息未加载，请重新登录')
    router.push('/')
    return
  }

  const token = localStorage.getItem('token')
  if (token) {
    connectSocket(token)
    chatSocket.value?.addEventListener('message', handleSocketMessage)
  }

  await fetchConversations()
  await handlePrivateChatFromQuery()

  if (!currentConversation.value && conversationList.value.length > 0) {
    await handleSelectConversation(conversationList.value[0])
  }
})

onUnmounted(() => {
  if (chatSocket.value) {
    chatSocket.value.removeEventListener('message', handleSocketMessage)
  }
})
</script>

<template>
  <div class="chat-page">
    <div class="chat-shell">
      <!-- 左侧：会话列表 -->
      <aside
          class="sidebar"
          :class="{ 'mobile-hidden': isMobile && currentConversation }"
      >
        <ChatSidebar
            :conversations="conversationList"
            :selected-id="currentConversation?.id"
            @select-conversation="handleSelectConversation"
        />
      </aside>

      <!-- 右侧：聊天区 -->
      <section class="chat-panel">
        <template v-if="currentConversation">
          <header class="chat-header">
            <el-button v-if="isMobile" text class="back-btn" @click="currentConversation = null">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <el-avatar :src="currentContact.avatar" :size="38" />
            <div class="contact-info">
              <div class="contact-name">{{ currentContact.name }}</div>
              <div class="contact-sub">私信</div>
            </div>
            <div class="header-actions">
              <el-tooltip content="视频通话" placement="bottom">
                <button type="button" class="icon-btn" @click="openVideoCall">
                  <el-icon size="18"><VideoCamera /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </header>

          <div ref="messagesRef" class="messages-scroll">
            <ChatMessages
                :messages="displayedMessages"
                :self-id="CURRENT_USER_ID"
                :contact="currentContact"
            />
          </div>

          <ChatInput
              :disabled="!currentConversation"
              @send="handleSendMessage"
              @insert-image="handleInsertImage"
              @start-video-call="openVideoCall"
          />
        </template>

        <div v-else class="empty-placeholder">
          <div class="empty-icon">
            <el-icon size="40"><ChatDotRound /></el-icon>
          </div>
          <h3>选择一个会话</h3>
          <p>从左侧列表选择好友开始聊天</p>
        </div>
      </section>
    </div>

    <VideoCallModal
        v-model="isVideoCalling"
        ref="videoCallModalRef"
        :contact-name="currentContact.name"
        :conversation-id="currentConversation?.id || 0"
        :target-user-id="currentContact.id || 0"
        @end-call="closeVideoCall"
    />
  </div>
</template>

<style scoped>
.chat-page {
  height: calc(100vh - 60px);
  background: #f3f5f9;
  padding: 12px;
  box-sizing: border-box;
}

.chat-shell {
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.08);
  border: 1px solid #e8ebf0;
}

.sidebar {
  width: 300px;
  flex-shrink: 0;
  border-right: 1px solid #eef0f4;
  background: #fafbfc;
  min-height: 0;
  transition: width 0.2s ease;
}

.sidebar.mobile-hidden {
  width: 0 !important;
  overflow: hidden;
  border-right: none;
}

.chat-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 18px;
  border-bottom: 1px solid #eef0f4;
  background: #fff;
  flex-shrink: 0;
  min-height: 64px;
  box-sizing: border-box;
}

.back-btn {
  margin-left: -6px;
}

.contact-info {
  flex: 1;
  min-width: 0;
}

.contact-name {
  font-size: 15px;
  font-weight: 600;
  color: #18191c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #61666d;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: #f0f2f5;
  color: #6c5ce7;
}

.messages-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  background: #f7f8fa;
}

.empty-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
  color: #94a3b8;
}

.empty-icon {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: #ece9fe;
  color: #6c5ce7;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.empty-placeholder h3 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #303133;
}

.empty-placeholder p {
  margin: 0;
  font-size: 13px;
}

@media (max-width: 768px) {
  .chat-page {
    padding: 0;
    height: calc(100vh - 60px);
  }

  .chat-shell {
    border-radius: 0;
    border: none;
    box-shadow: none;
  }

  .sidebar {
    position: absolute;
    left: 0;
    top: 60px;
    bottom: 56px;
    z-index: 20;
    width: 100%;
    border-right: none;
  }

  .sidebar.mobile-hidden {
    display: none;
  }
}
</style>

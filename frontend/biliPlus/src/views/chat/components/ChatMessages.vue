<!-- src/views/chat/components/ChatMessages.vue -->
<template>
  <div class="msg-list">
    <el-empty v-if="!messages.length" description="暂无消息，打个招呼吧" :image-size="72" />

    <template v-else>
      <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-row"
          :class="{ self: msg.isSelf }"
      >
        <el-avatar
            class="msg-avatar"
            :size="36"
            :src="msg.isSelf ? selfAvatar : contactAvatar"
        />
        <div class="msg-body">
          <div class="bubble">
            <span v-if="msg.msgType === 1" class="text">{{ msg.content }}</span>
            <img
                v-else-if="msg.msgType === 2"
                :src="msg.content"
                class="image"
                alt="图片消息"
            />
            <span v-else class="text">{{ msg.content }}</span>
          </div>
          <div class="time">{{ formatTime(msg.createdAt) }}</div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import defaultAvatar from '@/assets/default-avatar.jpg'
import { useUserStore } from '@/store/user.js'

const props = defineProps({
  messages: { type: Array, default: () => [] },
  selfId: { type: [Number, String, null], default: null },
  contact: { type: Object, default: () => ({}) },
})

const userStore = useUserStore()

const selfAvatar = computed(() => userStore.userInfo?.avatar || defaultAvatar)
const contactAvatar = computed(() => props.contact?.avatar || defaultAvatar)

const formatTime = (timeValue) => {
  if (!timeValue) return ''
  let date
  if (typeof timeValue === 'number') {
    date = new Date(timeValue)
  } else if (typeof timeValue === 'string') {
    let t = timeValue
    if (!t.endsWith('Z') && !/[+\-]\d{2}:?\d{2}$/.test(t)) {
      t += '+08:00'
    }
    date = new Date(t)
  } else {
    date = timeValue
  }
  if (isNaN(date.getTime())) return ''
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.msg-list {
  padding: 20px 18px 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

.msg-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  max-width: 78%;
}

.msg-row.self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.08);
}

.msg-body {
  min-width: 0;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  background: #fff;
  color: #18191c;
  font-size: 14px;
  line-height: 1.55;
  word-break: break-word;
  white-space: pre-wrap;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef0f4;
}

.msg-row:not(.self) .bubble {
  border-top-left-radius: 4px;
}

.msg-row.self .bubble {
  background: #6c5ce7;
  color: #fff;
  border-color: #6c5ce7;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 10px rgba(108, 92, 231, 0.28);
}

.image {
  max-width: 220px;
  max-height: 220px;
  border-radius: 8px;
  display: block;
  object-fit: cover;
}

.time {
  margin-top: 4px;
  font-size: 11px;
  color: #94a3b8;
}

.msg-row.self .time {
  text-align: right;
}
</style>

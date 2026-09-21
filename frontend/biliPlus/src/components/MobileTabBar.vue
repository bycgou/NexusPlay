<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getUnreadTotal } from '@/api/chat'
import { getNotificationUnreadCount } from '@/api/notify'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const tabs = [
  { path: '/', icon: 'HomeFilled', label: '首页', needAuth: false, showDot: false },
  { path: '/dynamic', icon: 'Promotion', label: '动态', needAuth: false, showDot: false },
  { path: '/contribute', icon: 'VideoCameraFilled', label: '投稿', needAuth: true, showDot: false },
  { path: '/chat', icon: 'ChatDotRound', label: '消息', needAuth: true, showDot: true },
  { path: '/setting', icon: 'User', label: '我的', needAuth: true, showDot: false }
]

const MSG_INDEX = 3

// 消息入口红点 = 私信未读 + 通知未读
const unreadMsg = ref(0)
const unreadNotify = ref(0)
const mergedUnread = computed(() => unreadMsg.value + unreadNotify.value)
let unreadTimer: ReturnType<typeof setInterval> | null = null

const refreshUnread = async () => {
  if (!userStore.isLogin) {
    unreadMsg.value = 0
    unreadNotify.value = 0
    return
  }
  try {
    const [msg, notify]: any[] = await Promise.all([getUnreadTotal(), getNotificationUnreadCount()])
    if (msg?.code === 1) unreadMsg.value = Number(msg.data) || 0
    if (notify?.code === 1) unreadNotify.value = Number(notify.data) || 0
  } catch (e) {
    // 静默失败，避免刷屏
  }
}

const startUnreadPolling = () => {
  stopUnreadPolling()
  refreshUnread()
  unreadTimer = setInterval(refreshUnread, 30000)
}

const stopUnreadPolling = () => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
}

const activeTab = computed(() => {
  const path = route.path
  if (path === '/') return 0
  if (path.startsWith('/dynamic')) return 1
  if (path.startsWith('/contribute')) return 2
  // 通知中心归属消息入口，避免选中态丢失
  if (path.startsWith('/chat') || path.startsWith('/notifications')) return MSG_INDEX
  if (path.startsWith('/setting')) return 4
  return 0
})

const handleTabClick = (index: number) => {
  const tab = tabs[index]

  // 需要登录的页面
  if (tab.needAuth && !userStore.isLogin) {
    router.push('/login')
    return
  }

  // 进消息入口时先清零红点，由后续轮询纠正
  if (index === MSG_INDEX) {
    unreadMsg.value = 0
    unreadNotify.value = 0
  }

  router.push(tab.path)
}

onMounted(() => {
  if (userStore.isLogin) startUnreadPolling()
})

onUnmounted(() => {
  stopUnreadPolling()
})

// 登录/退出后同步轮询状态
watch(
  () => userStore.isLogin,
  (login) => {
    if (login) startUnreadPolling()
    else {
      stopUnreadPolling()
      unreadMsg.value = 0
      unreadNotify.value = 0
    }
  }
)
</script>

<template>
  <nav class="mobile-tab-bar">
    <div
        v-for="(tab, index) in tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: activeTab === index }"
        @click="handleTabClick(index)"
    >
      <el-icon :size="22"><component :is="tab.icon" /></el-icon>
      <span class="tab-label">{{ tab.label }}</span>
      <span v-if="tab.showDot && mergedUnread > 0 && activeTab !== index" class="tab-dot"></span>
    </div>
  </nav>
</template>

<style scoped>
.mobile-tab-bar {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--paper-white);
  border-top: 1px solid var(--line);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  z-index: 1000;
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  color: var(--mist);
  transition: color var(--transition-fast);
  position: relative;
  min-height: 44px;
}

.tab-item:active {
  transform: scale(0.95);
}

.tab-item.active {
  color: var(--brand);
}

.tab-label {
  font-size: 10px;
  font-weight: 500;
}

.tab-dot {
  position: absolute;
  top: 8px;
  right: calc(50% - 14px);
  width: 8px;
  height: 8px;
  background: var(--like);
  border-radius: 50%;
  border: 2px solid var(--paper-white);
}

/* 移动端显示 */
@media (max-width: 768px) {
  .mobile-tab-bar {
    display: flex;
  }
}
</style>

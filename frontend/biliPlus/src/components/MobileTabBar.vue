<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const tabs = [
  { path: '/', icon: 'HomeFilled', label: '首页' },
  { path: '/contribute', icon: 'VideoCameraFilled', label: '投稿' },
  { path: '/chat', icon: 'ChatDotRound', label: '消息' },
  { path: '/setting', icon: 'User', label: '我的' }
]

const activeTab = computed(() => {
  const path = route.path
  if (path === '/') return 0
  if (path.startsWith('/contribute')) return 1
  if (path.startsWith('/chat')) return 2
  if (path.startsWith('/setting')) return 3
  return 0
})

const handleTabClick = (index: number) => {
  const tab = tabs[index]

  // 需要登录的页面
  if ((index === 1 || index === 2 || index === 3) && !userStore.isLogin) {
    router.push('/login')
    return
  }

  router.push(tab.path)
}
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
      <span v-if="index === 2 && activeTab !== index" class="tab-dot"></span>
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

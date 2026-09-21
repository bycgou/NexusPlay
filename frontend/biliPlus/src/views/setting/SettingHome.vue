<script setup lang="ts">
import {
  UserFilled,
  Film,
  Lock,
  VideoPlay,
  Monitor,
  Bell,
  View,
  Key
} from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const defaultAvatar = ref('https://picsum.photos/200/200')
const activeTab = ref((route.name as string) || 'profile')

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/')
}

const handleMenuChange = (index: string) => {
  activeTab.value = index
  router.push({ name: index })
}

watch(
  () => route.name,
  (name) => {
    if (name) activeTab.value = name as string
  }
)
</script>

<template>
  <div class="settings-layout">
    <el-container>
      <el-header class="settings-header">
        <div class="header-content">
          <el-button text type="primary" @click="goBack">返回上一页</el-button>
          <h2 class="page-title">账户设置</h2>
          <div class="header-right">
            <el-avatar :size="32" :src="userStore.userInfo.avatar || defaultAvatar" />
            <span class="user-name">{{ userStore.userInfo.nickname || '游客' }}</span>
          </div>
        </div>
      </el-header>

      <el-container class="body-container">
        <el-aside class="aside-content" width="220px">
          <el-menu
            :default-active="activeTab"
            @select="handleMenuChange"
            active-text-color="var(--brand)"
            background-color="transparent"
            text-color="var(--ink)"
            class="settings-menu"
          >
            <el-menu-item index="profile">
              <el-icon><UserFilled /></el-icon>
              <span>个人资料设置</span>
            </el-menu-item>
            <el-menu-item index="videoSet">
              <el-icon><Film /></el-icon>
              <span>稿件设置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>账号安全</span>
            </el-menu-item>
            <el-menu-item index="player">
              <el-icon><VideoPlay /></el-icon>
              <span>播放设置</span>
            </el-menu-item>
            <el-menu-item index="quality">
              <el-icon><Monitor /></el-icon>
              <span>画质与缓存</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon><Bell /></el-icon>
              <span>通知设置</span>
            </el-menu-item>
            <el-menu-item index="privacy">
              <el-icon><View /></el-icon>
              <span>隐私设置</span>
            </el-menu-item>
            <el-menu-item index="shortcut">
              <el-icon><Key /></el-icon>
              <span>快捷键设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="main-content">
          <div class="panel-card">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.settings-layout {
  min-height: 100vh;
  background: var(--paper);
}

.settings-header {
  background: var(--paper-white);
  border-bottom: 1px solid var(--line);
  padding: 0;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--ink);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right .user-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
  color: var(--ink-secondary);
  font-size: 14px;
}

.body-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  min-height: calc(100vh - 60px);
}

.aside-content {
  border-right: 1px solid var(--line);
  background: var(--paper-white);
  padding: 12px 8px;
  min-height: calc(100vh - 60px);
}

.settings-menu {
  border-right: none;
}

.settings-menu .el-menu-item {
  border-radius: var(--radius-md);
  margin: 2px 4px;
  height: 44px;
  line-height: 44px;
}

.settings-menu .el-menu-item:hover {
  background: var(--brand-bg);
}

.settings-menu .el-menu-item.is-active {
  background: var(--brand-bg);
  font-weight: 600;
}

.main-content {
  padding: 20px 24px 40px;
  background: var(--paper);
}

.panel-card {
  background: var(--paper-white);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

@media (max-width: 768px) {
  .aside-content {
    width: 72px !important;
  }

  .settings-menu :deep(span) {
    display: none;
  }

  .main-content {
    padding: 12px;
  }

  .panel-card {
    padding: 16px;
  }
}
</style>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const adminName = computed(() => authStore.userInfo?.name || '管理员')
const pageTitle = computed(() => {
  const t = route.meta?.title as string
  return t || '管理后台'
})

function handleLogout() {
  authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<template>
  <div class="header-inner">
    <div class="left">
      <span class="page-name">{{ pageTitle }}</span>
    </div>
    <div class="right">
      <el-avatar :size="32" class="avatar" src="https://www.loliapi.com/acg/pc/" />
      <span class="name">{{ adminName }}</span>
      <el-button type="danger" size="small" round @click="handleLogout">退出</el-button>
    </div>
  </div>
</template>

<style scoped>
.header-inner {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.name {
  font-size: 14px;
  color: #606266;
}
.avatar {
  background: #f0f2f5;
}
</style>

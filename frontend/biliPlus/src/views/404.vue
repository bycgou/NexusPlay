<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="not-found">
    <div class="content">
      <h1 class="error-code">404</h1>
      <p class="error-text">页面未找到</p>
      <p class="error-desc">抱歉，你访问的页面不存在或已被移除</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--paper);
}

.content {
  text-align: center;
}

.error-code {
  font-size: 96px;
  font-weight: 800;
  color: var(--brand);
  margin: 0;
  line-height: 1;
  letter-spacing: -4px;
}

.error-text {
  font-size: 24px;
  font-weight: 600;
  color: var(--ink);
  margin: var(--space-md) 0 var(--space-sm);
}

.error-desc {
  font-size: 14px;
  color: var(--mist);
  margin: 0 0 var(--space-xl);
}
</style>

<!-- src/components/NotFound.vue -->
<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <!-- 404 图标/文字 -->
      <div class="error-code">404</div>

      <!-- 提示信息 -->
      <div class="error-message">
        <p>抱歉，您访问的页面不存在或已被移除</p>
        <p class="sub-message">可能是输入的地址有误，或该页面已被删除</p>
      </div>

      <!-- 操作按钮 -->
      <div class="error-actions">
        <el-button
            type="primary"
            size="large"
            @click="handleGoBack"
        >
          <el-icon><ArrowLeft /></el-icon>
          返回上一页
        </el-button>
        <el-button
            size="large"
            @click="handleGoHome"
            style="margin-left: 16px"
        >
          <el-icon><Home /></el-icon>
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { useRouter } from 'vue-router'

const router = useRouter()

// 返回上一页
const handleGoBack = () => {
  window.history.back()
}

// 返回首页
const handleGoHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 60px 40px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.error-code {
  font-size: 80px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 24px;
  letter-spacing: 8px;
}

.error-message {
  margin-bottom: 32px;
}

.error-message p {
  font-size: 18px;
  color: #303133;
  margin: 0 0 8px 0;
}

.sub-message {
  font-size: 14px;
  color: #606266;
}

.error-actions {
  margin-top: 20px;
}
</style>
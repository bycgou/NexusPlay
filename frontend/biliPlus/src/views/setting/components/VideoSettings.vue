<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyVideos } from '@/api/myVideo'

const loading = ref(false)
const videos = ref<any[]>([])

const statusText = (s?: number) => {
  if (s === 0) return '审核中'
  if (s === 1) return '已通过'
  if (s === 2) return '已下架'
  if (s === 3) return '未通过'
  return '未知'
}

const statusType = (s?: number) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  if (s === 2) return 'info'
  if (s === 3) return 'danger'
  return 'info'
}

const loadVideos = async () => {
  loading.value = true
  try {
    const res = await getMyVideos()
    if (res.code === 1) {
      videos.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadVideos)
</script>

<template>
  <div class="my-videos">
    <div class="head">
      <h3>我的稿件</h3>
      <el-button size="small" @click="loadVideos">刷新</el-button>
    </div>

    <el-empty v-if="!loading && videos.length === 0" description="还没有投稿" />

    <div v-else class="list" v-loading="loading">
      <div v-for="item in videos" :key="item.id" class="card">
        <img :src="item.coverUrl" class="cover" alt="" />
        <div class="info">
          <div class="title-row">
            <span class="title">{{ item.title }}</span>
            <el-tag size="small" :type="statusType(item.status)">{{ statusText(item.status) }}</el-tag>
          </div>
          <div class="meta">
            投稿时间：{{ item.createTime || '—' }}
          </div>
          <el-alert
              v-if="item.status === 3 && item.rejectReason"
              type="error"
              :closable="false"
              show-icon
              class="reject"
          >
            <template #title>审核未通过</template>
            {{ item.rejectReason }}
          </el-alert>
          <div v-else-if="item.status === 0" class="tip">视频正在审核中，请耐心等待</div>
          <div v-else-if="item.status === 1" class="tip ok">视频已通过审核并展示</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-videos {
  padding: 8px 4px;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.head h3 {
  margin: 0;
  font-size: 16px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.card {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fff;
}
.cover {
  width: 140px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  background: #f5f7fa;
  flex-shrink: 0;
}
.info {
  flex: 1;
  min-width: 0;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}
.reject {
  border-radius: 6px;
}
.tip {
  font-size: 12px;
  color: #909399;
}
.tip.ok {
  color: #67c23a;
}
</style>

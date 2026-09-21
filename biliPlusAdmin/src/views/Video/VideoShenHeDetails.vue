<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  getAdminVideoDetail,
  approveVideo,
  offlineVideo,
  rejectVideo,
  type AdminVideo
} from '@/api/videoShenHe'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const videoId = route.params.id as string

const videoInfo = ref<AdminVideo | null>(null)
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectLoading = ref(false)

const statusText = (s?: number) => {
  if (s === 0) return '待审核'
  if (s === 1) return '已通过'
  if (s === 2) return '已下架'
  if (s === 3) return '审核不通过'
  return '未知'
}

const statusType = (s?: number) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  if (s === 2) return 'info'
  if (s === 3) return 'danger'
  return 'info'
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getAdminVideoDetail(videoId)
    videoInfo.value = res.data
  } catch (e) {
    console.error(e)
    ElMessage.error('加载视频详情失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async () => {
  if (!videoInfo.value) return
  try {
    await ElMessageBox.confirm('确认通过该视频审核？', '提示', { type: 'warning' })
    await approveVideo(videoInfo.value.id)
    ElMessage.success('审核通过')
    await loadDetail()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const openReject = () => {
  rejectReason.value = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  if (!videoInfo.value) return
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写不通过原因')
    return
  }
  rejectLoading.value = true
  try {
    await rejectVideo(videoInfo.value.id, rejectReason.value.trim())
    ElMessage.success('已标记为不通过')
    rejectVisible.value = false
    await loadDetail()
  } catch (e) {
    console.error(e)
  } finally {
    rejectLoading.value = false
  }
}

const handleOffline = async () => {
  if (!videoInfo.value) return
  try {
    await ElMessageBox.confirm('确认下架该视频？', '提示', { type: 'warning' })
    await offlineVideo(videoInfo.value.id)
    ElMessage.success('已下架')
    await loadDetail()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const goBack = () => {
  router.push('/Home/VideoShenHe')
}

onMounted(loadDetail)
</script>

<template>
  <div class="audit-page" v-loading="loading">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <h2 class="page-title">视频审核详情</h2>
    </div>

    <template v-if="videoInfo">
      <el-row :gutter="20" class="main-content">
        <el-col :span="14">
          <div class="video-container">
            <video
                v-if="videoInfo.videoUrl"
                class="video-player"
                :src="videoInfo.videoUrl"
                controls
                :poster="videoInfo.coverUrl"
            ></video>
            <div class="video-placeholder" v-else>
              <el-empty description="暂无视频地址" />
            </div>
            <div class="video-status">
              <el-tag :type="statusType(videoInfo.status)">{{ statusText(videoInfo.status) }}</el-tag>
            </div>
          </div>

          <el-alert
              v-if="videoInfo.status === 3 && videoInfo.rejectReason"
              class="reject-alert"
              type="error"
              :closable="false"
              show-icon
          >
            <template #title>不通过原因</template>
            {{ videoInfo.rejectReason }}
          </el-alert>
        </el-col>

        <el-col :span="10">
          <el-card shadow="never" class="info-card">
            <div class="video-title">{{ videoInfo.title }}</div>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="视频ID">{{ videoInfo.id }}</el-descriptions-item>
              <el-descriptions-item label="投稿用户ID">{{ videoInfo.userId }}</el-descriptions-item>
              <el-descriptions-item label="分类ID">{{ videoInfo.categoryId }}</el-descriptions-item>
              <el-descriptions-item label="投稿时间">{{ videoInfo.createTime || '—' }}</el-descriptions-item>
              <el-descriptions-item label="时长">{{ videoInfo.duration || '—' }}</el-descriptions-item>
              <el-descriptions-item label="播放量">{{ videoInfo.viewCount ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="点赞数">{{ videoInfo.likeCount ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="封面地址">
                <span class="url-text">{{ videoInfo.coverUrl || '无' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="视频地址">
                <span class="url-text">{{ videoInfo.videoUrl || '无' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="简介">
                <p class="video-desc">{{ videoInfo.description || '无简介' }}</p>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <div class="audit-actions">
        <el-button
            v-if="videoInfo.status === 0"
            type="success"
            size="large"
            @click="handleApprove"
        >通过审核</el-button>
        <el-button
            v-if="videoInfo.status === 0"
            type="danger"
            size="large"
            @click="openReject"
        >审核不通过</el-button>
        <el-button
            v-if="videoInfo.status === 1"
            type="warning"
            size="large"
            @click="handleOffline"
        >下架视频</el-button>
        <el-button size="large" @click="goBack">返回列表</el-button>
      </div>
    </template>
    <el-empty v-else-if="!loading" description="未找到视频" />

    <el-dialog v-model="rejectVisible" title="审核不通过" width="480px" :close-on-click-modal="false">
      <div class="reject-hint">请填写不通过原因，用户可在「设置 → 稿件」中查看。</div>
      <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="例如：内容违规 / 画质过低 / 标题与内容不符"
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="submitReject">确认不通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.audit-page {
  padding: 16px 20px 28px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.main-content {
  margin-top: 12px;
}
.video-container {
  background: #000;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}
.video-player {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: contain;
  background: #000;
  display: block;
}
.video-placeholder {
  width: 100%;
  aspect-ratio: 16/9;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}
.video-status {
  padding: 10px 14px;
  background: #fff;
}
.reject-alert {
  margin-top: 12px;
  border-radius: 8px;
}
.info-card {
  border-radius: 10px;
}
.video-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 14px;
  line-height: 1.5;
  color: #303133;
}
.video-desc {
  white-space: pre-line;
  line-height: 1.6;
  color: #606266;
  margin: 0;
}
.url-text {
  word-break: break-all;
  font-size: 12px;
  color: #606266;
}
.audit-actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
}
.reject-hint {
  margin-bottom: 12px;
  color: #909399;
  font-size: 13px;
}
</style>

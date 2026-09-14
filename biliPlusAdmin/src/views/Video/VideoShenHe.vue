<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getAdminVideoPage,
  approveVideo,
  offlineVideo,
  rejectVideo,
  type AdminVideo
} from '@/api/videoShenHe'

const router = useRouter()
const loading = ref(false)
const tableData = ref<AdminVideo[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
// 0待审 1正常 2下架 3不通过；null=全部
const status = ref<number | null>(0)

// 拒绝弹窗
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref<AdminVideo | null>(null)
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

const loadList = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, pageSize: pageSize.value }
    if (status.value !== null) params.status = status.value
    const res = await getAdminVideoPage(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleStatusChange = (val: number | null) => {
  status.value = val
  page.value = 1
  loadList()
}

const handlePageChange = (p: number) => {
  page.value = p
  loadList()
}

const handleSizeChange = (s: number) => {
  pageSize.value = s
  page.value = 1
  loadList()
}

const handleDetail = (id: number) => {
  router.push(`/Home/VideoShenHe/Detail/${id}`)
}

const handleApprove = async (row: AdminVideo) => {
  try {
    await approveVideo(row.id)
    ElMessage.success('已通过审核')
    loadList()
  } catch (e) {
    console.error(e)
  }
}

const handleOffline = async (row: AdminVideo) => {
  try {
    await offlineVideo(row.id)
    ElMessage.success('已下架')
    loadList()
  } catch (e) {
    console.error(e)
  }
}

const openReject = (row: AdminVideo) => {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  if (!rejectTarget.value) return
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写不通过原因')
    return
  }
  rejectLoading.value = true
  try {
    await rejectVideo(rejectTarget.value.id, rejectReason.value.trim())
    ElMessage.success('已标记为不通过')
    rejectVisible.value = false
    loadList()
  } catch (e) {
    console.error(e)
  } finally {
    rejectLoading.value = false
  }
}

onMounted(loadList)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-radio-group :model-value="status" @update:model-value="handleStatusChange">
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已过审</el-radio-button>
        <el-radio-button :value="3">不通过</el-radio-button>
        <el-radio-button :value="2">已下架</el-radio-button>
        <el-radio-button :value="null">全部</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="loadList">刷新</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading" class="table-card">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="封面" width="140">
        <template #default="scope">
          <el-image
              v-if="scope.row.coverUrl"
              :src="scope.row.coverUrl"
              class="cover-img"
              fit="cover"
          />
          <div v-else class="cover-empty">无封面</div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userId" label="投稿用户ID" width="110" />
      <el-table-column prop="categoryId" label="分类ID" width="90" />
      <el-table-column prop="createTime" label="投稿时间" width="170" />
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="不通过原因" min-width="140" show-overflow-tooltip>
        <template #default="scope">
          <span class="reason" v-if="scope.row.status === 3 && scope.row.rejectReason">
            {{ scope.row.rejectReason }}
          </span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="handleDetail(scope.row.id)">查看</el-button>
          <el-button
              v-if="scope.row.status === 0"
              type="success"
              link
              @click="handleApprove(scope.row)"
          >通过</el-button>
          <el-button
              v-if="scope.row.status === 0"
              type="danger"
              link
              @click="openReject(scope.row)"
          >不通过</el-button>
          <el-button
              v-if="scope.row.status === 1"
              type="warning"
              link
              @click="handleOffline(scope.row)"
          >下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          :page-sizes="[10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="rejectVisible" title="审核不通过" width="480px" :close-on-click-modal="false">
      <div class="reject-hint">
        请填写不通过原因，用户可在「设置 → 稿件」中查看。
      </div>
      <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="例如：视频内容涉及违规 / 画质过低 / 标题党，请修改后重新投稿"
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="submitReject">确认不通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 16px 20px;
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: wrap;
}
.table-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.cover-img {
  width: 100px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}
.cover-empty {
  width: 100px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
  border-radius: 6px;
}
.reason {
  color: #f56c6c;
  font-size: 13px;
}
.muted {
  color: #c0c4cc;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.reject-hint {
  margin-bottom: 12px;
  color: #909399;
  font-size: 13px;
}
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReports, handleReport, type Report } from '@/api/report'

const loading = ref(false)
const list = ref<Report[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
// '' 表示「全部」
const status = ref<number | ''>(0)
const targetType = ref<number | ''>('')

const targetTypeText = (t?: number) => {
  if (t === 1) return '视频'
  if (t === 2) return '评论'
  if (t === 3) return '弹幕'
  if (t === 4) return '用户'
  if (t === 5) return '直播间'
  return '未知'
}

const reasonText = (r?: number) => {
  if (r === 1) return '违法'
  if (r === 2) return '色情'
  if (r === 3) return '辱骂'
  if (r === 4) return '广告'
  if (r === 5) return '其他'
  return '未知'
}

const statusText = (s?: number) => {
  if (s === 0) return '待处理'
  if (s === 1) return '已处理'
  if (s === 2) return '已驳回'
  return '未知'
}

const statusTagType = (s?: number) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  return 'info'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getReports({
      status: status.value === '' ? undefined : status.value,
      targetType: targetType.value === '' ? undefined : targetType.value,
      page: page.value,
      size: size.value
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 接口异常已由 request 拦截器弹出提示，这里仅留调试信息
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onFilter = () => {
  page.value = 1
  loadList()
}

const onPageChange = (p: number) => {
  page.value = p
  loadList()
}

const onSizeChange = (s: number) => {
  size.value = s
  page.value = 1
  loadList()
}

// 处理弹窗
const dialogVisible = ref(false)
const current = ref<Report | null>(null)
const handleStatus = ref<number>(1)
const remark = ref('')
const submitting = ref(false)

const openHandle = (row: Report) => {
  current.value = row
  handleStatus.value = 1
  remark.value = ''
  dialogVisible.value = true
}

const submitHandle = async () => {
  if (!current.value) return
  submitting.value = true
  try {
    await handleReport(current.value.id, {
      status: handleStatus.value,
      remark: remark.value.trim() || undefined
    })
    ElMessage.success(handleStatus.value === 1 ? '已判定成立并联动处置' : '已驳回该举报')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(loadList)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="status" placeholder="处理状态" style="width: 140px" @change="onFilter">
        <el-option label="全部状态" :value="''" />
        <el-option label="待处理" :value="0" />
        <el-option label="已处理" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
      <el-select v-model="targetType" placeholder="举报对象" style="width: 140px" @change="onFilter">
        <el-option label="全部对象" :value="''" />
        <el-option label="视频" :value="1" />
        <el-option label="评论" :value="2" />
        <el-option label="弹幕" :value="3" />
        <el-option label="用户" :value="4" />
        <el-option label="直播间" :value="5" />
      </el-select>
      <el-button type="primary" @click="onFilter">查询</el-button>
      <el-button @click="loadList">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe empty-text="暂无举报记录">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="对象类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ targetTypeText(row.targetType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetId" label="对象ID" width="90" />
      <el-table-column label="举报原因" width="100">
        <template #default="{ row }">
          <el-tag size="small" type="danger">{{ reasonText(row.reason) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="detail" label="补充说明" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.detail">{{ row.detail }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="reporterId" label="举报人" width="90" />
      <el-table-column prop="createTime" label="举报时间" width="170" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleRemark" label="处理备注" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.handleRemark">{{ row.handleRemark }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="handleTime" label="处理时间" width="170">
        <template #default="{ row }">
          <span v-if="row.handleTime">{{ row.handleTime }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" link @click="openHandle(row)">处理</el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          :page-sizes="[10, 20, 50]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="处理举报" width="520px" :close-on-click-modal="false">
      <div v-if="current" class="detail-box">
        <div class="detail-line">
          举报 #{{ current?.id }} · {{ targetTypeText(current?.targetType) }} #{{ current?.targetId }} ·
          原因：{{ reasonText(current?.reason) }}
        </div>
        <div class="detail-line muted">补充说明：{{ current?.detail || '无' }}</div>
      </div>
      <el-radio-group v-model="handleStatus" class="status-group">
        <el-radio :value="1">举报成立（视频下架 / 评论删除）</el-radio>
        <el-radio :value="2">驳回举报</el-radio>
      </el-radio-group>
      <el-input
          v-model="remark"
          type="textarea"
          :rows="3"
          maxlength="255"
          show-word-limit
          placeholder="处理备注（成立时会作为通知内容发给作者，可留空）"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
            :type="handleStatus === 1 ? 'danger' : 'primary'"
            :loading="submitting"
            @click="submitHandle"
        >确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px 20px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.muted { color: #c0c4cc; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
.detail-box { margin-bottom: 12px; font-size: 13px; color: #606266; }
.detail-line { line-height: 1.8; }
.status-group { margin-bottom: 12px; display: block; }
</style>

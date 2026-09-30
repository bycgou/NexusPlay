<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOperationLogs, ACTION_OPTIONS, type OperationLog } from '@/api/operationLog'

const loading = ref(false)
const rows = ref<OperationLog[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const adminId = ref<number | null>(null)
const action = ref('')

const load = async () => {
  loading.value = true
  try {
    const res = await getOperationLogs({
      adminId: adminId.value === null ? undefined : adminId.value,
      action: action.value || undefined,
      page: page.value,
      size: size.value
    })
    rows.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onFilter = () => {
  page.value = 1
  load()
}

const actionText = (a?: string) => {
  const hit = ACTION_OPTIONS.find((o) => o.value === a)
  return hit ? hit.label : a || '—'
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-head">
      <h2>操作日志</h2>
      <el-button size="small" @click="load">刷新</el-button>
    </div>

    <div class="toolbar">
      <el-input
          v-model="adminId"
          placeholder="管理员ID"
          style="width: 140px"
          clearable
          @change="onFilter"
      />
      <el-select v-model="action" placeholder="操作类型" style="width: 180px" clearable @change="onFilter">
        <el-option v-for="o in ACTION_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe empty-text="暂无操作日志">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="管理员" width="100">
        <template #default="{ row }">
          {{ row.adminId ?? '系统' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-tag size="small">{{ actionText(row.action) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="action" label="动作码" width="160" />
      <el-table-column label="目标" width="160">
        <template #default="{ row }">
          {{ row.targetType ? `${row.targetType}#${row.targetId ?? '—'}` : '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="detail" label="详情" min-width="180" show-overflow-tooltip />
      <el-table-column prop="ip" label="来源 IP" width="140" />
    </el-table>

    <div class="pager">
      <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="(p: number) => { page = p; load() }"
      />
    </div>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.page-head h2 { margin: 0; font-size: 18px; }
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>

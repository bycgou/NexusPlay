<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getWalletTransactions, type WalletTransaction } from '@/api/walletTx'

const loading = ref(false)
const list = ref<WalletTransaction[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const userId = ref<number | undefined>()
const type = ref<number | ''>('')
const bizType = ref<string>('')
// 后端支持 YYYY-MM-DD 或完整时间字符串，按天传值时后端自动补全时分秒
const from = ref<string>('')
const to = ref<string>('')

const typeText = (t?: number) => {
  if (t === 1) return '充值'
  if (t === 2) return '送礼支出'
  if (t === 3) return '主播收入'
  if (t === 4) return '系统调整'
  return '未知'
}

const typeTagType = (t?: number) => {
  if (t === 1) return 'success'
  if (t === 2) return 'warning'
  if (t === 3) return 'primary'
  return 'info'
}

const amountText = (v?: number | null) => {
  if (v === undefined || v === null) return '—'
  return v > 0 ? `+${v}` : `${v}`
}

const amountClass = (v?: number) => ((v ?? 0) < 0 ? 'amount-out' : 'amount-in')

const loadList = async () => {
  loading.value = true
  try {
    const res = await getWalletTransactions({
      userId: userId.value || undefined,
      type: type.value === '' ? undefined : type.value,
      bizType: bizType.value || undefined,
      from: from.value.trim() || undefined,
      to: to.value.trim() || undefined,
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

const onSearch = () => {
  page.value = 1
  loadList()
}

const onReset = () => {
  userId.value = undefined
  type.value = ''
  bizType.value = ''
  from.value = ''
  to.value = ''
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

onMounted(loadList)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model.number="userId" placeholder="用户ID" clearable style="width: 140px" />
      <el-select v-model="type" placeholder="账变类型" style="width: 140px">
        <el-option label="全部类型" :value="''" />
        <el-option label="充值" :value="1" />
        <el-option label="送礼支出" :value="2" />
        <el-option label="主播收入" :value="3" />
        <el-option label="系统调整" :value="4" />
      </el-select>
      <el-select v-model="bizType" placeholder="业务类型" clearable style="width: 150px">
        <el-option label="充值" value="recharge" />
        <el-option label="送礼" value="gift" />
        <el-option label="主播收入" value="host_income" />
        <el-option label="系统调整" value="adjust" />
      </el-select>
      <el-input v-model="from" placeholder="开始日期 YYYY-MM-DD" clearable style="width: 180px" />
      <el-input v-model="to" placeholder="结束日期 YYYY-MM-DD" clearable style="width: 180px" />
      <el-button type="primary" @click="onSearch">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>

    <div class="hint">
      金额单位：硬币（1 元 = 100 硬币）；变动后余额的口径随业务类型变化，主播收入为累计收益，其余为钱包余额。
    </div>

    <el-table v-loading="loading" :data="list" border stripe empty-text="暂无账变记录">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTagType(row.type)">{{ typeText(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="变动金额" width="110">
        <template #default="{ row }">
          <span :class="amountClass(row.amount)">{{ amountText(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="balanceAfter" label="变动后余额" width="120" />
      <el-table-column prop="bizType" label="业务类型" width="120" />
      <el-table-column prop="bizId" label="关联业务ID" width="110">
        <template #default="{ row }">
          <span v-if="row.bizId">{{ row.bizId }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.remark">{{ row.remark }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="账变时间" width="170" />
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
  </div>
</template>

<style scoped>
.page { padding: 16px 20px; }
.toolbar { margin-bottom: 12px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.hint { margin-bottom: 12px; font-size: 12px; color: #909399; }
.muted { color: #c0c4cc; }
.amount-in { color: #67c23a; font-weight: 600; }
.amount-out { color: #f56c6c; font-weight: 600; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>

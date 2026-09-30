<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSensitiveWords,
  createSensitiveWord,
  updateSensitiveWord,
  deleteSensitiveWord,
  getSensitiveHits,
  getHitSummary,
  reviewSensitiveHit,
  getPenalties,
  createPenalty,
  releasePenalty,
  type SensitiveWord,
  type SensitiveHit,
  type Penalty,
  type HitSummary
} from '@/api/sensitive'

// ---- 命中统计 ----
const summaryLoading = ref(false)
const summary = ref<HitSummary | null>(null)

const levelText = (l?: number) => {
  if (l === 1) return '拦截'
  if (l === 2) return '转人工'
  if (l === 3) return '仅标记'
  return '未知'
}

const levelTagType = (l?: number) => {
  if (l === 1) return 'danger'
  if (l === 2) return 'warning'
  return 'info'
}

const wordStatusText = (s?: number) => (s === 1 ? '启用' : '停用')
const wordStatusTagType = (s?: number) => (s === 1 ? 'success' : 'info')

const hitActionText = (a?: string) => (a === 'block' ? '拦截' : a === 'mark' ? '标记' : '未知')
const hitActionTagType = (a?: string) => (a === 'block' ? 'danger' : 'warning')

const reviewText = (s?: number) => {
  if (s === 0) return '待复核'
  if (s === 1) return '确认违规'
  if (s === 2) return '误伤'
  return '未知'
}

const reviewTagType = (s?: number) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'danger'
  return 'success'
}

const penaltyActionText = (a?: string) => (a === 'mute' ? '禁言' : a === 'ban' ? '封禁' : '未知')
const penaltyStatusText = (s?: number) => {
  if (s === 1) return '生效中'
  if (s === 2) return '已到期'
  if (s === 3) return '已提前解除'
  return '未知'
}

const penaltyStatusTagType = (s?: number) => {
  if (s === 1) return 'danger'
  if (s === 2) return 'info'
  return 'success'
}

const endTimeText = (row: Penalty) => (row.endTime ? row.endTime : '永久')

const rateText = (v?: number | null) => {
  if (v === undefined || v === null) return '—'
  const pct = v <= 1 ? v * 100 : v
  return `${pct.toFixed(1)}%`
}

const loadSummary = async () => {
  summaryLoading.value = true
  try {
    const res = await getHitSummary()
    summary.value = res.data ?? null
  } catch (e) {
    console.error(e)
  } finally {
    summaryLoading.value = false
  }
}

// ---- 词库 ----
const wordLoading = ref(false)
const wordList = ref<SensitiveWord[]>([])
const wordTotal = ref(0)
const wordPage = ref(1)
const wordSize = ref(20)
const wordKeyword = ref('')
const wordStatus = ref<number | ''>('')

const loadWords = async () => {
  wordLoading.value = true
  try {
    const res = await getSensitiveWords({
      keyword: wordKeyword.value.trim() || undefined,
      status: wordStatus.value === '' ? undefined : wordStatus.value,
      page: wordPage.value,
      size: wordSize.value
    })
    wordList.value = res.data?.records || []
    wordTotal.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    wordLoading.value = false
  }
}

const onWordFilter = () => {
  wordPage.value = 1
  loadWords()
}

const onWordPageChange = (p: number) => {
  wordPage.value = p
  loadWords()
}

const onWordSizeChange = (s: number) => {
  wordSize.value = s
  wordPage.value = 1
  loadWords()
}

const wordDialogVisible = ref(false)
const editingWordId = ref<number | null>(null)
const wordForm = ref({ word: '', level: 1, status: 1 })
const wordSubmitting = ref(false)

const openWordCreate = () => {
  editingWordId.value = null
  wordForm.value = { word: '', level: 1, status: 1 }
  wordDialogVisible.value = true
}

const openWordEdit = (row: SensitiveWord) => {
  editingWordId.value = row.id
  wordForm.value = { word: row.word, level: row.level, status: row.status }
  wordDialogVisible.value = true
}

const submitWord = async () => {
  const word = wordForm.value.word.trim()
  if (!word) {
    ElMessage.warning('请输入敏感词')
    return
  }
  wordSubmitting.value = true
  try {
    if (editingWordId.value) {
      await updateSensitiveWord(editingWordId.value, {
        level: wordForm.value.level,
        status: wordForm.value.status
      })
      ElMessage.success('修改成功')
    } else {
      await createSensitiveWord({ word, level: wordForm.value.level })
      ElMessage.success('新增成功')
    }
    wordDialogVisible.value = false
    loadWords()
  } catch (e) {
    console.error(e)
  } finally {
    wordSubmitting.value = false
  }
}

const handleWordDelete = async (row: SensitiveWord) => {
  try {
    await ElMessageBox.confirm(`确认删除敏感词「${row.word}」？`, '提示', { type: 'warning' })
    await deleteSensitiveWord(row.id)
    ElMessage.success('删除成功')
    loadWords()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

// ---- 命中记录 ----
const hitLoading = ref(false)
const hitList = ref<SensitiveHit[]>([])
const hitTotal = ref(0)
const hitPage = ref(1)
const hitSize = ref(20)
const hitReviewStatus = ref<number | ''>('')
const hitAction = ref<string>('')

const loadHits = async () => {
  hitLoading.value = true
  try {
    const res = await getSensitiveHits({
      reviewStatus: hitReviewStatus.value === '' ? undefined : hitReviewStatus.value,
      action: hitAction.value || undefined,
      page: hitPage.value,
      size: hitSize.value
    })
    hitList.value = res.data?.records || []
    hitTotal.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    hitLoading.value = false
  }
}

const onHitFilter = () => {
  hitPage.value = 1
  loadHits()
}

const onHitPageChange = (p: number) => {
  hitPage.value = p
  loadHits()
}

const onHitSizeChange = (s: number) => {
  hitSize.value = s
  hitPage.value = 1
  loadHits()
}

const reviewSubmitting = ref(false)

const reviewHit = async (row: SensitiveHit, reviewStatus: 1 | 2) => {
  const label = reviewStatus === 1 ? '确认违规' : '误伤'
  try {
    await ElMessageBox.confirm(`确认将命中 #${row.id} 复核为「${label}」？`, '提示', {
      type: 'warning'
    })
    reviewSubmitting.value = true
    await reviewSensitiveHit(row.id, { reviewStatus })
    ElMessage.success('复核完成')
    loadHits()
    loadSummary()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  } finally {
    reviewSubmitting.value = false
  }
}

// ---- 处置记录 ----
const penLoading = ref(false)
const penList = ref<Penalty[]>([])
const penTotal = ref(0)
const penPage = ref(1)
const penSize = ref(20)
const penUserId = ref<number | undefined>()
const penStatus = ref<number | ''>('')

const loadPenalties = async () => {
  penLoading.value = true
  try {
    const res = await getPenalties({
      userId: penUserId.value || undefined,
      status: penStatus.value === '' ? undefined : penStatus.value,
      page: penPage.value,
      size: penSize.value
    })
    penList.value = res.data?.records || []
    penTotal.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    penLoading.value = false
  }
}

const onPenFilter = () => {
  penPage.value = 1
  loadPenalties()
}

const onPenPageChange = (p: number) => {
  penPage.value = p
  loadPenalties()
}

const onPenSizeChange = (s: number) => {
  penSize.value = s
  penPage.value = 1
  loadPenalties()
}

const penDialogVisible = ref(false)
const penForm = ref({
  userId: undefined as number | undefined,
  action: 'mute' as 'mute' | 'ban',
  reason: '',
  daysText: ''
})
const penSubmitting = ref(false)

const openPenaltyCreate = (userId?: number) => {
  penForm.value = {
    userId,
    action: 'mute',
    reason: '',
    daysText: ''
  }
  penDialogVisible.value = true
}

const submitPenalty = async () => {
  const uid = penForm.value.userId
  if (!uid) {
    ElMessage.warning('请输入用户ID')
    return
  }
  const reason = penForm.value.reason.trim()
  if (!reason) {
    ElMessage.warning('请输入处置原因')
    return
  }
  let days: number | undefined
  const daysRaw = penForm.value.daysText.trim()
  if (daysRaw) {
    const n = Number(daysRaw)
    if (!Number.isInteger(n) || n <= 0) {
      ElMessage.warning('天数需为正整数，或留空表示永久')
      return
    }
    days = n
  }
  penSubmitting.value = true
  try {
    await createPenalty({
      userId: uid,
      action: penForm.value.action,
      reason,
      days
    })
    ElMessage.success('处置已下发')
    penDialogVisible.value = false
    loadPenalties()
  } catch (e) {
    console.error(e)
  } finally {
    penSubmitting.value = false
  }
}

const handleRelease = async (row: Penalty) => {
  try {
    await ElMessageBox.confirm(
      `确认提前解除用户 #${row.userId} 的${penaltyActionText(row.action)}？`,
      '提示',
      { type: 'warning' }
    )
    await releasePenalty({ userId: row.userId, action: row.action as 'mute' | 'ban' })
    ElMessage.success('已提前解除')
    loadPenalties()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const refreshAll = () => {
  loadSummary()
  loadWords()
  loadHits()
  loadPenalties()
}

onMounted(refreshAll)
</script>

<template>
  <div class="page">
    <!-- 命中统计 -->
    <div v-loading="summaryLoading" class="stat-row">
      <div class="stat-card">
        <div class="stat-label">命中总数</div>
        <div class="stat-value">{{ summary?.total ?? '—' }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已复核</div>
        <div class="stat-value">{{ summary?.reviewed ?? '—' }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">确认违规</div>
        <div class="stat-value danger">{{ summary?.confirmed ?? '—' }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">误伤</div>
        <div class="stat-value warn">{{ summary?.falsePositive ?? '—' }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">误伤率</div>
        <div class="stat-value">{{ rateText(summary?.falsePositiveRate) }}</div>
      </div>
    </div>

    <!-- 词库管理 -->
    <div class="section-title">词库管理</div>
    <div class="toolbar">
      <el-input
          v-model="wordKeyword"
          placeholder="敏感词关键词"
          clearable
          style="width: 180px"
          @keyup.enter="onWordFilter"
      />
      <el-select v-model="wordStatus" placeholder="状态" style="width: 120px" @change="onWordFilter">
        <el-option label="全部状态" :value="''" />
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="onWordFilter">查询</el-button>
      <el-button type="success" @click="openWordCreate">新增敏感词</el-button>
      <el-button @click="loadWords">刷新</el-button>
    </div>

    <el-table v-loading="wordLoading" :data="wordList" border stripe empty-text="暂无敏感词">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="word" label="敏感词" min-width="160" />
      <el-table-column label="级别" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="levelTagType(row.level)">{{ levelText(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="wordStatusTagType(row.status)">{{ wordStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openWordEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleWordDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="wordTotal"
          :page-size="wordSize"
          :current-page="wordPage"
          :page-sizes="[10, 20, 50]"
          @current-change="onWordPageChange"
          @size-change="onWordSizeChange"
      />
    </div>

    <!-- 命中记录 -->
    <div class="section-title">命中记录</div>
    <div class="toolbar">
      <el-select v-model="hitReviewStatus" placeholder="复核状态" style="width: 140px" @change="onHitFilter">
        <el-option label="全部复核" :value="''" />
        <el-option label="待复核" :value="0" />
        <el-option label="确认违规" :value="1" />
        <el-option label="误伤" :value="2" />
      </el-select>
      <el-select v-model="hitAction" placeholder="处置动作" style="width: 120px" @change="onHitFilter">
        <el-option label="全部动作" value="" />
        <el-option label="拦截" value="block" />
        <el-option label="标记" value="mark" />
      </el-select>
      <el-button type="primary" @click="onHitFilter">查询</el-button>
      <el-button @click="loadHits">刷新</el-button>
    </div>

    <el-table v-loading="hitLoading" :data="hitList" border stripe empty-text="暂无命中记录">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="word" label="敏感词" width="120" show-overflow-tooltip />
      <el-table-column label="级别" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="levelTagType(row.level)">{{ levelText(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户" width="90" />
      <el-table-column prop="targetType" label="对象类型" width="100" />
      <el-table-column prop="targetId" label="对象ID" width="90" />
      <el-table-column prop="content" label="命中内容" min-width="160" show-overflow-tooltip />
      <el-table-column label="动作" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="hitActionTagType(row.action)">{{ hitActionText(row.action) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="复核" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="reviewTagType(row.reviewStatus)">{{ reviewText(row.reviewStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="命中时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.reviewStatus === 0">
            <el-button
                type="danger"
                link
                :disabled="reviewSubmitting"
                @click="reviewHit(row, 1)"
            >确认违规</el-button>
            <el-button
                type="success"
                link
                :disabled="reviewSubmitting"
                @click="reviewHit(row, 2)"
            >误伤</el-button>
          </template>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="hitTotal"
          :page-size="hitSize"
          :current-page="hitPage"
          :page-sizes="[10, 20, 50]"
          @current-change="onHitPageChange"
          @size-change="onHitSizeChange"
      />
    </div>

    <!-- 处置记录 -->
    <div class="section-title">处置记录</div>
    <div class="toolbar">
      <el-input
          v-model.number="penUserId"
          placeholder="用户ID"
          clearable
          style="width: 140px"
      />
      <el-select v-model="penStatus" placeholder="状态" style="width: 130px" @change="onPenFilter">
        <el-option label="全部状态" :value="''" />
        <el-option label="生效中" :value="1" />
        <el-option label="已到期" :value="2" />
        <el-option label="已提前解除" :value="3" />
      </el-select>
      <el-button type="primary" @click="onPenFilter">查询</el-button>
      <el-button type="warning" @click="openPenaltyCreate()">发起处置</el-button>
      <el-button @click="loadPenalties">刷新</el-button>
    </div>

    <el-table v-loading="penLoading" :data="penList" border stripe empty-text="暂无处置记录">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column label="动作" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.action === 'ban' ? 'danger' : 'warning'">
            {{ penaltyActionText(row.action) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="140" show-overflow-tooltip />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column label="结束时间" width="170">
        <template #default="{ row }">
          <span v-if="row.endTime">{{ row.endTime }}</span>
          <span v-else class="muted">{{ endTimeText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="penaltyStatusTagType(row.status)">{{ penaltyStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
              v-if="row.status === 1"
              type="warning"
              link
              @click="handleRelease(row)"
          >提前解除</el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="penTotal"
          :page-size="penSize"
          :current-page="penPage"
          :page-sizes="[10, 20, 50]"
          @current-change="onPenPageChange"
          @size-change="onPenSizeChange"
      />
    </div>

    <!-- 词库编辑弹窗 -->
    <el-dialog
        v-model="wordDialogVisible"
        :title="editingWordId ? '编辑敏感词' : '新增敏感词'"
        width="480px"
        :close-on-click-modal="false"
     append-to-body>
      <el-form label-width="90px">
        <el-form-item label="敏感词" required>
          <el-input
              v-model="wordForm.word"
              placeholder="敏感词内容"
              maxlength="50"
              :disabled="!!editingWordId"
          />
        </el-form-item>
        <el-form-item label="级别" required>
          <el-select v-model="wordForm.level" style="width: 100%">
            <el-option label="拦截" :value="1" />
            <el-option label="转人工" :value="2" />
            <el-option label="仅标记" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="editingWordId" label="状态">
          <el-select v-model="wordForm.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="wordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="wordSubmitting" @click="submitWord">保存</el-button>
      </template>
    </el-dialog>

    <!-- 发起处置弹窗 -->
    <el-dialog v-model="penDialogVisible" title="发起处置" width="480px" :close-on-click-modal="false" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="用户ID" required>
          <el-input v-model.number="penForm.userId" placeholder="目标用户ID" />
        </el-form-item>
        <el-form-item label="动作" required>
          <el-radio-group v-model="penForm.action">
            <el-radio value="mute">禁言</el-radio>
            <el-radio value="ban">封禁</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input
              v-model="penForm.reason"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
              placeholder="处置原因"
          />
        </el-form-item>
        <el-form-item label="天数">
          <el-input v-model="penForm.daysText" placeholder="留空表示永久" style="width: 160px" />
          <span class="muted" style="margin-left: 8px">正整数，留空永久</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="penDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="penSubmitting" @click="submitPenalty">确认处置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px 20px; }
.toolbar { margin-bottom: 12px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.muted { color: #c0c4cc; }
.pager { margin-top: 12px; margin-bottom: 8px; display: flex; justify-content: flex-end; }
.section-title {
  margin: 20px 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-left: 3px solid #2563EB;
  padding-left: 8px;
}
.stat-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.stat-card {
  flex: 1;
  min-width: 120px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px 16px;
}
.stat-label { font-size: 12px; color: #909399; margin-bottom: 6px; }
.stat-value { font-size: 22px; font-weight: 700; color: #303133; }
.stat-value.danger { color: #f56c6c; }
.stat-value.warn { color: #e6a23c; }
</style>

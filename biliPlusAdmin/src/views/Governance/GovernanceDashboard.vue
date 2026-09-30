<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getGovernanceOverview,
  getReportSla,
  getTimeliness,
  getTopViolators,
  getViolationExposure,
  type GovernanceOverview,
  type ReportSla,
  type TimelinessRow,
  type TopViolator,
  type ViolationExposure,
  type ExposureRow
} from '@/api/governance'

const overviewLoading = ref(false)
const slaLoading = ref(false)
const timeLoading = ref(false)
const topLoading = ref(false)
const exposureLoading = ref(false)

const overview = ref<GovernanceOverview | null>(null)
const sla = ref<ReportSla | null>(null)
const timeRows = ref<TimelinessRow[]>([])
const days = ref(7)
const topRows = ref<TopViolator[]>([])
const exposure = ref<ViolationExposure | null>(null)
const exposureRows = ref<ExposureRow[]>([])
const exposureDays = ref(7)

const rateText = (v?: number | null) => {
  if (v === undefined || v === null) return '—'
  const pct = v <= 1 ? v * 100 : v
  return `${pct.toFixed(1)}%`
}

// 误伤率 / 复发率（分母为 0 时显示 —）
const falsePositiveRate = computed(() => {
  const s = overview.value?.sensitiveHit
  if (!s) return null
  const reviewed = s.confirmed + s.falsePositive
  if (!reviewed) return null
  return s.falsePositive / reviewed
})

const recidivismRate = computed(() => {
  const p = overview.value?.penaltyRecidivism
  if (!p || !p.penalizedUsers) return null
  return p.recidivistUsers / p.penalizedUsers
})

const maxCnt = computed(() => {
  let m = 1
  for (const r of timeRows.value) {
    if (r.cnt > m) m = r.cnt
  }
  return m
})

const barWidth = (cnt: number) => `${Math.round((cnt / maxCnt.value) * 100)}%`

const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const res = await getGovernanceOverview()
    overview.value = res.data ?? null
  } catch (e) {
    console.error(e)
  } finally {
    overviewLoading.value = false
  }
}

const loadSla = async () => {
  slaLoading.value = true
  try {
    const res = await getReportSla()
    sla.value = res.data ?? null
  } catch (e) {
    console.error(e)
  } finally {
    slaLoading.value = false
  }
}

const loadTimeliness = async () => {
  timeLoading.value = true
  try {
    const res = await getTimeliness(days.value)
    timeRows.value = res.data?.rows || []
  } catch (e) {
    console.error(e)
  } finally {
    timeLoading.value = false
  }
}

const loadTop = async () => {
  topLoading.value = true
  try {
    const res = await getTopViolators(10)
    topRows.value = res.data?.rows || []
  } catch (e) {
    console.error(e)
  } finally {
    topLoading.value = false
  }
}

const loadExposure = async () => {
  exposureLoading.value = true
  try {
    const res = await getViolationExposure(exposureDays.value)
    exposure.value = res.data ?? null
    exposureRows.value = res.data?.rows || []
  } catch (e) {
    console.error(e)
  } finally {
    exposureLoading.value = false
  }
}

const refreshAll = () => {
  loadOverview()
  loadSla()
  loadTimeliness()
  loadTop()
  loadExposure()
}

onMounted(refreshAll)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="days" style="width: 140px" @change="loadTimeliness">
        <el-option label="近 7 日" :value="7" />
        <el-option label="近 14 日" :value="14" />
        <el-option label="近 30 日" :value="30" />
      </el-select>
      <el-button @click="refreshAll">刷新</el-button>
    </div>

    <!-- KPI -->
    <div v-loading="overviewLoading" class="kpi-row">
      <div class="kpi-card">
        <div class="kpi-label">总举报</div>
        <div class="kpi-value">{{ overview?.report?.total ?? '—' }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">待处理</div>
        <div class="kpi-value warn">{{ overview?.report?.pending ?? '—' }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">超时</div>
        <div class="kpi-value danger">{{ overview?.report?.overdue ?? '—' }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">平均处理时长(分)</div>
        <div class="kpi-value">{{ overview?.report?.avgHandleMinutes ?? '—' }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">敏感词误伤率</div>
        <div class="kpi-value">{{ rateText(falsePositiveRate) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">处置复发率</div>
        <div class="kpi-value">{{ rateText(recidivismRate) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">低信用用户</div>
        <div class="kpi-value">{{ overview?.lowCreditUsers ?? '—' }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">违规曝光率</div>
        <div class="kpi-value">{{ rateText(overview?.exposure?.rate) }}</div>
      </div>
    </div>

    <!-- 举报 SLA -->
    <div class="section-title">举报 SLA</div>
    <div v-loading="slaLoading" class="sla-grid">
      <div class="sla-item">
        <span class="sla-label">成立</span>
        <span class="sla-num">{{ sla?.summary?.upheld ?? '—' }}</span>
      </div>
      <div class="sla-item">
        <span class="sla-label">驳回</span>
        <span class="sla-num">{{ sla?.summary?.rejected ?? '—' }}</span>
      </div>
      <div class="sla-item">
        <span class="sla-label">待处理</span>
        <span class="sla-num">{{ sla?.summary?.pending ?? '—' }}</span>
      </div>
      <div class="sla-item">
        <span class="sla-label">超时</span>
        <span class="sla-num">{{ sla?.summary?.overdue ?? '—' }}</span>
      </div>
      <div class="sla-item">
        <span class="sla-label">平均时长(分)</span>
        <span class="sla-num">{{ sla?.summary?.avgHandleMinutes ?? '—' }}</span>
      </div>
    </div>

    <div class="sub-title">处理人工作量</div>
    <el-table
        v-loading="slaLoading"
        :data="sla?.handlerWorkload || []"
        border
        stripe
        empty-text="暂无处理记录"
    >
      <el-table-column prop="handlerId" label="处理人ID" width="120" />
      <el-table-column prop="cnt" label="处理量" width="120" />
      <el-table-column prop="upheld" label="成立数" width="120" />
      <el-table-column label="成立率" min-width="120">
        <template #default="{ row }">
          {{ row.cnt ? rateText(row.upheld / row.cnt) : '—' }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 时效趋势 -->
    <div class="section-title">处理时效趋势</div>
    <div v-loading="timeLoading" class="trend-box">
      <el-table :data="timeRows" border stripe empty-text="暂无趋势数据">
        <el-table-column prop="day" label="日期" width="120" />
        <el-table-column prop="cnt" label="处理量" width="100" />
        <el-table-column prop="avgHandleMinutes" label="平均时长(分)" width="120" />
        <el-table-column label="处理量分布" min-width="180">
          <template #default="{ row }">
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: barWidth(row.cnt) }" />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 违规曝光率：治理效果的核心指标，用于介入前后对比 -->
    <div class="section-title">
      违规曝光率趋势
      <el-select v-model="exposureDays" style="width: 140px" @change="loadExposure">
        <el-option :value="7" label="近 7 天" />
        <el-option :value="30" label="近 30 天" />
        <el-option :value="90" label="近 90 天" />
      </el-select>
    </div>
    <div v-loading="exposureLoading" class="trend-box">
      <div class="sla-grid">
        <div class="sla-item">
          <span class="sla-label">总曝光</span>
          <span class="sla-num">{{ exposure?.totalViews ?? '—' }}</span>
        </div>
        <div class="sla-item">
          <span class="sla-label">违规曝光</span>
          <span class="sla-num">{{ exposure?.violationViews ?? '—' }}</span>
        </div>
        <div class="sla-item">
          <span class="sla-label">曝光率</span>
          <span class="sla-num">{{ rateText(exposure?.rate) }}</span>
        </div>
        <div class="sla-item">
          <span class="sla-label">违规视频数</span>
          <span class="sla-num">{{ exposure?.violationVideoCount ?? '—' }}</span>
        </div>
      </div>
      <el-table :data="exposureRows" border stripe empty-text="暂无曝光数据（需先积累 event_log）">
        <el-table-column prop="day" label="日期" width="120" />
        <el-table-column prop="totalViews" label="总曝光" width="100" />
        <el-table-column prop="violationViews" label="违规曝光" width="100" />
        <el-table-column label="曝光率" width="100">
          <template #default="{ row }">
            {{ row.totalViews ? rateText(row.violationViews / row.totalViews) : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="违规曝光分布" min-width="180">
          <template #default="{ row }">
            <div class="bar-track">
              <div class="bar-fill violation" :style="{ width: barWidth(row.violationViews) }" />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Top 违规用户 -->
    <div class="section-title">Top 违规用户</div>
    <el-table
        v-loading="topLoading"
        :data="topRows"
        border
        stripe
        empty-text="暂无违规用户"
    >
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="nickname" label="昵称" min-width="120">
        <template #default="{ row }">
          <span v-if="row.nickname">{{ row.nickname }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" min-width="120">
        <template #default="{ row }">
          <span v-if="row.username">{{ row.username }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="信用分" width="100" />
      <el-table-column prop="violationCount" label="违规次数" width="100" />
      <el-table-column prop="lastViolationTime" label="最近违规" width="170">
        <template #default="{ row }">
          <span v-if="row.lastViolationTime">{{ row.lastViolationTime }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page { padding: 16px 20px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.muted { color: #c0c4cc; }
.section-title {
  margin: 20px 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-left: 3px solid #2563EB;
  padding-left: 8px;
}
.sub-title {
  margin: 12px 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}
.kpi-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.kpi-card {
  flex: 1;
  min-width: 120px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px;
}
.kpi-label { font-size: 12px; color: #909399; margin-bottom: 8px; }
.kpi-value { font-size: 22px; font-weight: 700; color: #303133; }
.kpi-value.danger { color: #f56c6c; }
.kpi-value.warn { color: #e6a23c; }
.sla-grid {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.sla-item {
  flex: 1;
  min-width: 100px;
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.sla-label { font-size: 12px; color: #909399; }
.sla-num { font-size: 18px; font-weight: 600; color: #303133; }
.bar-track {
  width: 100%;
  height: 10px;
  background: #ebeef5;
  border-radius: 5px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: #2563EB;
  border-radius: 5px;
  min-width: 2px;
}
/* 违规曝光用警示色，与普通处理量区分开 */
.bar-fill.violation {
  background: #f56c6c;
}
</style>

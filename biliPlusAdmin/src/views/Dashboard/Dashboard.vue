<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getStatsOverview,
  getStatsTrend,
  type StatsOverview,
  type TrendPoint
} from '@/api/stats'

const loading = ref(false)
const trendLoading = ref(false)
const overview = ref<StatsOverview | null>(null)
const days = ref(7)
const videoTrend = ref<TrendPoint[]>([])
const userTrend = ref<TrendPoint[]>([])
const giftTrend = ref<TrendPoint[]>([])

const fmt = (v?: number | null) => (v === undefined || v === null ? '—' : String(v))

const maxOf = (rows: TrendPoint[], key: 'cnt' | 'amount') => {
  let m = 0
  for (const r of rows) {
    const v = Number(r[key] ?? 0)
    if (v > m) m = v
  }
  return m || 1
}

const barWidth = (value: number, max: number) => `${Math.round((value / max) * 100)}%`

const loadOverview = async () => {
  loading.value = true
  try {
    const res = await getStatsOverview()
    overview.value = res.data ?? null
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadTrend = async () => {
  trendLoading.value = true
  try {
    const res = await getStatsTrend(days.value)
    videoTrend.value = res.data?.videos || []
    userTrend.value = res.data?.users || []
    giftTrend.value = res.data?.gifts || []
  } catch (e) {
    console.error(e)
  } finally {
    trendLoading.value = false
  }
}

const refreshAll = () => {
  loadOverview()
  loadTrend()
}

onMounted(refreshAll)
</script>

<template>
  <div class="page">
    <div class="page-head">
      <h2>数据看板</h2>
      <el-button size="small" @click="refreshAll">刷新</el-button>
    </div>

    <div v-loading="loading" class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-label">用户数</div>
        <div class="kpi-value">{{ fmt(overview?.totalUsers) }}</div>
        <div class="kpi-sub">今日 +{{ fmt(overview?.todayUsers) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">投稿数</div>
        <div class="kpi-value">{{ fmt(overview?.totalVideos) }}</div>
        <div class="kpi-sub">今日 +{{ fmt(overview?.todayVideos) }}</div>
      </div>
      <div class="kpi-card highlight">
        <div class="kpi-label">待审核</div>
        <div class="kpi-value">{{ fmt(overview?.pendingVideos) }}</div>
        <div class="kpi-sub">需要处理</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">在线直播</div>
        <div class="kpi-value">{{ fmt(overview?.liveRooms) }}</div>
        <div class="kpi-sub">当前开播</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">礼物收入</div>
        <div class="kpi-value">{{ fmt(overview?.totalGiftAmount) }}</div>
        <div class="kpi-sub">今日 +{{ fmt(overview?.todayGiftAmount) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">弹幕数</div>
        <div class="kpi-value">{{ fmt(overview?.totalDanmaku) }}</div>
        <div class="kpi-sub">今日 +{{ fmt(overview?.todayDanmaku) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">评论数</div>
        <div class="kpi-value">{{ fmt(overview?.totalComments) }}</div>
        <div class="kpi-sub">有效评论</div>
      </div>
      <div class="kpi-card highlight">
        <div class="kpi-label">待处理举报</div>
        <div class="kpi-value">{{ fmt(overview?.pendingReports) }}</div>
        <div class="kpi-sub">行为流水 {{ fmt(overview?.totalEvents) }} 条</div>
      </div>
    </div>

    <div class="section-title">
      近 N 日趋势
      <el-select v-model="days" style="width: 140px" @change="loadTrend">
        <el-option :value="7" label="近 7 天" />
        <el-option :value="30" label="近 30 天" />
        <el-option :value="90" label="近 90 天" />
      </el-select>
    </div>

    <div v-loading="trendLoading" class="trend-grid">
      <div class="trend-card">
        <div class="trend-title">新增投稿</div>
        <el-table :data="videoTrend" size="small" border stripe empty-text="暂无数据">
          <el-table-column prop="day" label="日期" width="110" />
          <el-table-column prop="cnt" label="投稿" width="80" />
          <el-table-column label="分布">
            <template #default="{ row }">
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: barWidth(Number(row.cnt || 0), maxOf(videoTrend, 'cnt')) }" />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="trend-card">
        <div class="trend-title">新增注册</div>
        <el-table :data="userTrend" size="small" border stripe empty-text="暂无数据">
          <el-table-column prop="day" label="日期" width="110" />
          <el-table-column prop="cnt" label="注册" width="80" />
          <el-table-column label="分布">
            <template #default="{ row }">
              <div class="bar-track">
                <div class="bar-fill green" :style="{ width: barWidth(Number(row.cnt || 0), maxOf(userTrend, 'cnt')) }" />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="trend-card">
        <div class="trend-title">礼物收入</div>
        <el-table :data="giftTrend" size="small" border stripe empty-text="暂无数据">
          <el-table-column prop="day" label="日期" width="110" />
          <el-table-column prop="amount" label="金额" width="80" />
          <el-table-column label="分布">
            <template #default="{ row }">
              <div class="bar-track">
                <div class="bar-fill gold" :style="{ width: barWidth(Number(row.amount || 0), maxOf(giftTrend, 'amount')) }" />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-head h2 { margin: 0; font-size: 18px; }
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}
.kpi-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px;
}
.kpi-card.highlight { border-color: #2563EB; }
.kpi-label { font-size: 12px; color: #909399; margin-bottom: 8px; }
.kpi-value { font-size: 24px; font-weight: 600; color: #303133; }
.kpi-sub { font-size: 12px; color: #909399; margin-top: 6px; }
.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  margin-bottom: 12px;
}
.trend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
.trend-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}
.trend-title { font-weight: 600; margin-bottom: 8px; }
.bar-track {
  width: 100%;
  height: 8px;
  background: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill { height: 100%; background: #2563EB; border-radius: 4px; min-width: 2px; }
.bar-fill.green { background: #67c23a; }
.bar-fill.gold { background: #f5a623; }
</style>

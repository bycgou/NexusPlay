<template>
  <div class="anime-page">
    <div class="page-hero">
      <div class="hero-inner">
        <p class="kicker">BANGUMI</p>
        <h1 class="title">番剧</h1>
        <p class="desc">连载新番、完结经典，一键追番</p>
        <div class="filters">
          <el-radio-group v-model="status" size="default" @change="resetAndLoad">
            <el-radio-button :value="undefined">全部</el-radio-button>
            <el-radio-button :value="1">连载中</el-radio-button>
            <el-radio-button :value="2">已完结</el-radio-button>
            <el-radio-button :value="0">未播出</el-radio-button>
          </el-radio-group>
          <el-input
              v-model="keyword"
              placeholder="搜索番剧名"
              clearable
              class="search"
              @keyup.enter="resetAndLoad"
              @clear="resetAndLoad"
          >
            <template #append>
              <el-button @click="resetAndLoad">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <div class="body">
      <div v-if="loading && list.length === 0" class="grid">
        <div v-for="i in 6" :key="i" class="card skeleton" />
      </div>

      <div v-else-if="list.length" class="grid">
        <div
            v-for="item in list"
            :key="item.id"
            class="card"
            @click="openDetail(item)"
        >
          <div class="cover-wrap">
            <img
                :src="item.coverUrl || fallbackCover(item.id)"
                :alt="item.title"
                class="cover"
            />
            <div class="cover-mask">
              <span class="score">{{ formatScore(item.score) }}</span>
              <el-button round size="small" type="primary" class="watch-btn">立即观看</el-button>
            </div>
            <span class="status-tag" :class="statusClass(item.status)">
              {{ statusText(item.status) }}
            </span>
          </div>
          <div class="info">
            <h3 class="name">{{ item.title }}</h3>
            <p class="meta">
              {{ item.airedEpisodes ?? 0 }}/{{ item.totalEpisodes ?? 0 }} 集
              · {{ item.followerCount || 0 }} 追番
            </p>
            <p class="desc">{{ item.description || '暂无简介' }}</p>
          </div>
        </div>
      </div>

      <EmptyState
          v-else
          icon="Film"
          title="暂无番剧"
          description="稍后再来，或去首页看看视频"
      />

      <div v-if="hasMore && list.length" class="load-more">
        <el-button :loading="loading" @click="loadMore">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnimeList } from '@/api/anime'
import EmptyState from '@/components/EmptyState.vue'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 12
const hasMore = ref(true)
const status = ref(undefined)
const keyword = ref('')

const fallbackCover = (id) =>
    `https://picsum.photos/320/420?random=anime${id}`

const statusText = (s) => {
  if (s === 0) return '未播出'
  if (s === 1) return '连载中'
  if (s === 2) return '已完结'
  return '未知'
}

const statusClass = (s) => ({
  s0: s === 0,
  s1: s === 1,
  s2: s === 2
})

const formatScore = (s) => {
  const n = Number(s)
  return Number.isFinite(n) ? n.toFixed(1) : '—'
}

const load = async (append = false) => {
  loading.value = true
  try {
    const params = { page: page.value, pageSize }
    if (status.value !== undefined && status.value !== null && status.value !== '') {
      params.status = status.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const res = await getAnimeList(params)
    if (res.code === 1 && res.data) {
      const records = res.data.records || []
      list.value = append ? [...list.value, ...records] : records
      hasMore.value = records.length >= pageSize
    } else {
      if (!append) list.value = []
      hasMore.value = false
    }
  } catch (e) {
    console.error(e)
    if (!append) list.value = []
  } finally {
    loading.value = false
  }
}

const resetAndLoad = () => {
  page.value = 1
  hasMore.value = true
  load(false)
}

const loadMore = () => {
  if (!hasMore.value || loading.value) return
  page.value += 1
  load(true)
}

const openDetail = (item) => {
  ElMessage.info(`《${item.title}》详情页开发中，可先在管理端维护剧集`)
}

onMounted(resetAndLoad)
</script>

<style scoped>
.anime-page {
  min-height: calc(100vh - 60px);
  background: var(--paper);
  padding-bottom: 48px;
}

.page-hero {
  background: linear-gradient(120deg, #0c4a6e 0%, #1d4ed8 50%, #7c3aed 100%);
  color: #fff;
  padding: 40px 16px 32px;
}

.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.kicker {
  margin: 0 0 6px;
  letter-spacing: 3px;
  font-size: 12px;
  opacity: 0.75;
}

.title {
  margin: 0 0 8px;
  font-size: 34px;
  font-weight: 800;
}

.desc {
  margin: 0 0 20px;
  opacity: 0.85;
  font-size: 14px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filters :deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.25);
  color: #fff;
}

.filters :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #fff;
  color: #1d4ed8;
  border-color: #fff;
}

.search {
  width: 260px;
}

.search :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.95);
}

.body {
  max-width: 1200px;
  margin: 24px auto 0;
  padding: 0 16px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 18px;
}

.card {
  background: var(--paper-white);
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid var(--line);
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.cover-wrap {
  position: relative;
  aspect-ratio: 3/4;
  overflow: hidden;
  background: var(--line-light);
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.card:hover .cover {
  transform: scale(1.05);
}

.cover-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.card:hover .cover-mask {
  opacity: 1;
}

.score {
  font-size: 28px;
  font-weight: 800;
  color: #fbbf24;
}

.status-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
}

.status-tag.s1 {
  background: #22c55e;
}

.status-tag.s2 {
  background: #64748b;
}

.status-tag.s0 {
  background: #f59e0b;
}

.info {
  padding: 12px 14px 14px;
}

.name {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  margin: 0 0 6px;
  font-size: 12px;
  color: var(--mist);
}

.desc {
  margin: 0;
  font-size: 12px;
  color: var(--mist);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 34px;
}

.skeleton {
  min-height: 320px;
  background: linear-gradient(90deg, var(--line-light), var(--paper), var(--line-light));
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.load-more {
  text-align: center;
  margin-top: 24px;
}

@media (max-width: 640px) {
  .search {
    width: 100%;
  }
}
</style>

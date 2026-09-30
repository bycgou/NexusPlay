<template>
  <section class="hot-rank-panel">
    <header class="panel-header">
      <h3 class="panel-title">
        <span class="title-dot"></span>
        热搜榜
      </h3>
      <span class="panel-sub">实时热度</span>
    </header>

    <div v-if="loading" class="panel-loading">加载中...</div>
    <div v-else-if="!list.length" class="panel-empty">暂无热榜</div>

    <ol v-else class="rank-list">
      <li
          v-for="item in list"
          :key="item.id"
          class="rank-item"
          @click="goVideo(item)"
      >
        <span class="rank-num" :class="rankClass(item.rank)">{{ item.rank }}</span>
        <div class="rank-body">
          <p class="rank-title" :title="item.title">{{ item.title }}</p>
          <p class="rank-meta">
            <span>{{ formatCount(item.viewCount) }} 播放</span>
            <span v-if="item.nickname">· {{ item.nickname }}</span>
          </p>
        </div>
      </li>
    </ol>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotVideos } from '@/api/video'

const router = useRouter()
const list = ref([])
const loading = ref(true)

const rankClass = (rank) => {
  if (rank === 1) return 'top-1'
  if (rank === 2) return 'top-2'
  if (rank === 3) return 'top-3'
  return ''
}

const formatCount = (num) => {
  const n = Number(num || 0)
  if (n >= 10000) {
    const val = n / 10000
    return val % 1 === 0 ? `${val}万` : `${val.toFixed(1)}万`
  }
  return String(n)
}

const goVideo = (item) => {
  if (item?.id) {
    router.push(`/video/${item.id}`)
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await getHotVideos(20)
    if (res.code === 1 && Array.isArray(res.data)) {
      list.value = res.data
    } else {
      list.value = []
    }
  } catch (e) {
    console.error('加载热榜失败', e)
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.hot-rank-panel {
  background: var(--surface, #fff);
  border: 1px solid var(--line, rgba(0, 0, 0, 0.06));
  border-radius: 12px;
  padding: 14px 14px 8px;
}

.panel-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
  flex-shrink: 0;
}

.panel-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink, #1f2329);
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-dot {
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--brand, #fb7299);
}

.panel-sub {
  font-size: 12px;
  color: var(--ink-tertiary, #8a919f);
}

.panel-loading,
.panel-empty {
  padding: 18px 4px;
  text-align: center;
  color: var(--ink-tertiary, #8a919f);
  font-size: 13px;
}

.rank-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.rank-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 8px 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.rank-item:hover {
  background: var(--paper, rgba(0, 0, 0, 0.04));
}

.rank-num {
  width: 18px;
  flex-shrink: 0;
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-tertiary, #8a919f);
  line-height: 1.5;
}

.rank-num.top-1 { color: #fb7299; }
.rank-num.top-2 { color: #ff8c4b; }
.rank-num.top-3 { color: #f2b124; }

.rank-body {
  min-width: 0;
  flex: 1;
}

.rank-title {
  margin: 0;
  font-size: 13px;
  line-height: 1.45;
  color: var(--ink, #1f2329);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rank-meta {
  margin: 2px 0 0;
  font-size: 11px;
  color: var(--ink-tertiary, #8a919f);
}
</style>

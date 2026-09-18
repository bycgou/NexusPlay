<template>
  <div class="channel-page">
    <div class="channel-hero" :class="channelType">
      <div class="hero-inner">
        <p class="hero-kicker">{{ meta.kicker }}</p>
        <h1 class="hero-title">{{ meta.title }}</h1>
        <p class="hero-desc">{{ meta.desc }}</p>
        <div class="hero-tags">
          <el-tag
              v-for="c in matchedCategories"
              :key="c.id"
              effect="plain"
              class="hero-tag"
              :class="{ active: activeCategoryId === c.id }"
              @click="selectCategory(c.id)"
          >
            {{ c.name }}
          </el-tag>
          <el-tag
              class="hero-tag"
              :class="{ active: activeCategoryId === null }"
              @click="selectCategory(null)"
          >
            全部
          </el-tag>
        </div>
      </div>
    </div>

    <div class="channel-body">
      <VideoList
          :video-data="videoList"
          :loading="loading"
          @view-more="loadMore"
      >
        <template #title>{{ meta.title }}</template>
        <template #video-item="{ item }">
          <VideoItem :item="item" />
        </template>
      </VideoList>

      <EmptyState
          v-if="!loading && videoList.length === 0"
          icon="VideoCamera"
          :title="`暂无${meta.title}内容`"
          :description="activeCategoryId ? '换一个分区试试，或稍后再来' : '等待 UP 主投稿相关内容'"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories } from '@/api/category'
import { getVideoList } from '@/api/video'
import VideoList from '@/views/home/Main/Video/VideoList.vue'
import VideoItem from '@/views/home/Main/Video/VideoItem.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const loading = ref(false)
const categories = ref([])
const videoList = ref([])
const activeCategoryId = ref(null)
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)

const channelType = computed(() => String(route.params.type || 'animation'))

const META = {
  animation: {
    title: '动画',
    kicker: 'ANIMATION',
    desc: '原创动画、MMD、手书与二创精选',
    keywords: ['动画', '动漫', '二次元', 'MMD', '手书']
  },
  movie: {
    title: '影视',
    kicker: 'MOVIE & TV',
    desc: '电影、剧集、预告片与影视解说',
    keywords: ['影视', '电影', '电视剧', '剧集', '纪录片', '预告']
  }
}

const meta = computed(() => META[channelType.value] || META.animation)

const matchedCategories = computed(() => {
  const kws = meta.value.keywords
  return categories.value.filter((c) => {
    const name = c?.name || ''
    return kws.some((k) => name.includes(k) || k.includes(name))
  })
})

const selectCategory = (id) => {
  activeCategoryId.value = id
  resetAndLoad()
}

const formatItem = (item) => ({
  id: item.id,
  coverUrl: item.coverUrl,
  title: item.title,
  author: item.nickname || 'UP主',
  duration: item.duration || '00:00',
  playCount: item.viewCount || 0,
  likeCount: item.likeCount || 0,
  createTime: item.createTime
})

const loadVideos = async (append = false) => {
  loading.value = true
  try {
    const params = { page: page.value, pageSize }
    if (activeCategoryId.value != null) {
      params.categoryId = activeCategoryId.value
    }
    const res = await getVideoList(params)
    if (res.code === 1 && Array.isArray(res.data?.records)) {
      const list = res.data.records.map(formatItem)
      videoList.value = append ? [...videoList.value, ...list] : list
      hasMore.value = list.length >= pageSize
    } else {
      if (!append) videoList.value = []
      hasMore.value = false
    }
  } catch (e) {
    console.error('加载频道视频失败', e)
    if (!append) videoList.value = []
  } finally {
    loading.value = false
  }
}

const resetAndLoad = () => {
  page.value = 1
  videoList.value = []
  hasMore.value = true
  loadVideos(false)
}

const loadMore = () => {
  if (!hasMore.value || loading.value) return
  page.value += 1
  loadVideos(true)
}

const loadCategories = async () => {
  try {
    const res = await getCategories(1)
    if (res.code === 1 && Array.isArray(res.data)) {
      categories.value = res.data
      // 默认选中第一个匹配分区；无匹配则展示全部分类下相关内容
      if (matchedCategories.value.length > 0) {
        activeCategoryId.value = matchedCategories.value[0].id
      } else {
        activeCategoryId.value = null
      }
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

watch(channelType, async () => {
  await loadCategories()
  resetAndLoad()
})

onMounted(async () => {
  await loadCategories()
  resetAndLoad()
})
</script>

<style scoped>
.channel-page {
  min-height: calc(100vh - 60px);
  background: var(--paper);
  padding-bottom: 40px;
}

.channel-hero {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 45%, #4c1d95 100%);
  color: #fff;
  padding: 36px 16px 28px;
}

.channel-hero.movie {
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 50%, #0e7490 100%);
}

.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-kicker {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 3px;
  opacity: 0.7;
  text-transform: uppercase;
}

.hero-title {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 1px;
}

.hero-desc {
  margin: 0 0 18px;
  font-size: 14px;
  opacity: 0.85;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-tag {
  cursor: pointer;
  border-color: rgba(255, 255, 255, 0.35);
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  transition: all var(--transition-fast);
}

.hero-tag:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.18);
}

.hero-tag.active {
  background: #fff;
  color: #312e81;
  border-color: #fff;
  font-weight: 600;
}

.channel-body {
  max-width: 1200px;
  margin: 0 auto;
}
</style>

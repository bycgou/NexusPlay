<template>
  <div class="page-container">
    <VideoList
        :video-data="videoList"
        :loading="loading"
        @view-more="handleViewMore"
    >
      <template #title>{{ categoryId ? '分类视频' : '热门推荐' }}</template>

      <template #video-item="{ item }">
        <VideoItem :item="item" />
      </template>
    </VideoList>
  </div>
</template>

<script setup>
import VideoItem from "@/views/home/Main/Video/VideoItem.vue";
import VideoList from "@/views/home/Main/Video/VideoList.vue";
import {getVideoList} from "@/api/video.js";
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  categoryId: {
    type: [Number, String],
    default: null
  }
})

const videoList = ref([])
const loading = ref(true)
const page = ref(1)
const pageSize = ref(20)

const formatVideoData = (resData) => {
  return resData.map(item => ({
    id: item.id,
    coverUrl: item.coverUrl,
    title: item.title,
    author: `${item.nickname || 'UP主'}`,
    duration: item.duration || '00:00',
    playCount: item.viewCount || 0,
    likeCount: item.likeCount || 0,
    createTime: item.createTime,
    tag: item.categoryId === 10 ? '原创' : '热门'
  }))
}

const loadVideoList = async (reset = false) => {
  loading.value = true
  try {
    if (reset) {
      page.value = 1
      videoList.value = []
    }
    const params = { page: page.value, pageSize: pageSize.value }
    if (props.categoryId != null && props.categoryId !== '') {
      params.categoryId = props.categoryId
    }
    const res = await getVideoList(params)
    if (res.code === 1 && Array.isArray(res.data?.records)) {
      const formattedData = formatVideoData(res.data.records)
      videoList.value.push(...formattedData)
    }
  } catch (err) {
    console.error('加载视频列表失败：', err)
  } finally {
    loading.value = false
  }
}

const handleViewMore = () => {
  page.value += 1
  loadVideoList()
}

watch(() => props.categoryId, () => {
  loadVideoList(true)
})

onMounted(() => {
  loadVideoList(true)
})
</script>

<style scoped>
.page-container {
  background-color: var(--paper);
  padding: var(--space-md) 0;
  min-height: calc(100vh - 60px);
}
</style>

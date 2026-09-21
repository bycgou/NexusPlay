<script setup lang="ts">
//<!--主容器,左侧视频+评论区,右侧推荐区-->
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import VideoPlayerWithDanmaku from './VideoPlayerWithDanmaku.vue'
import VideoInfoBar from './VideoInfoBar.vue'
import CommentSection from './CommentSection.vue'
import RecommendVideoSidebar from './RecommendVideoSidebar.vue'
import { getVideoDetail, getUserInfo, getRecommendVideos } from "@/api/video.ts";

// 获取路由参数
const route = useRoute()
const videoId = route.params.id

// 响应式数据
const currentVideo = ref({
  id: '',
  title: '',
  videoUrl: '',
  coverUrl: '',
  playCount: 0,
  duration: '',
  publishTime: '',
  author: {
    name: '',
    avatar: '',
    fansCount: 0
  }
})
const recommendVideos = ref([])

// 格式化时间
const formatTime = (timeArr) => {
  if (!timeArr || timeArr.length < 3) return '未知时间'
  return `${timeArr[0]}-${timeArr[1].toString().padStart(2, '0')}-${timeArr[2].toString().padStart(2, '0')}`
}

// 加载视频详情
const loadVideoDetail = async () => {
  try {
    const videoRes = await getVideoDetail(videoId)
    if (videoRes.data.code !== 1) throw new Error('获取视频详情失败')
    const videoData = videoRes.data.data

    const userRes = await getUserInfo(videoData.userId)
    if (userRes.code !== 1) throw new Error('获取作者信息失败')
    const userData = userRes.data

    currentVideo.value = {
      id: videoData.id || videoId,
      title: videoData.title,
      videoUrl: videoData.videoUrl,
      coverUrl: videoData.coverUrl,
      playCount: videoData.viewCount || 0,
      duration: videoData.duration || '00:00',
      publishTime: formatTime(videoData.createTime),
      author: {
        name: userData.nickname || `用户${videoData.userId}`,
        avatar: userData.avatar || `https://picsum.photos/100/100?random=${videoData.userId}`,
        fansCount: userData.fansCount || Math.floor(Math.random() * 100000)
      }
    }

    loadRecommendVideos()
  } catch (err) {
    console.error('加载视频详情失败：', err)
  }
}

// 加载推荐视频
const loadRecommendVideos = async () => {
  try {
    const res = await getRecommendVideos(videoId)
    if (res.code === 1) {
      recommendVideos.value = res.data.list.map(item => ({
        id: item.id || Math.random().toString(36).substr(2, 9),
        coverUrl: item.coverUrl,
        title: item.title,
        author: `用户${item.userId}`,
        duration: item.duration || `${Math.floor(Math.random() * 30)}:${Math.floor(Math.random() * 60).toString().padStart(2, '0')}`,
        playCount: item.viewCount || 0,
        likeCount: item.likeCount || 0,
        tag: item.categoryId === 10 ? '原创' : '热门'
      }))
    }
  } catch (err) {
    console.error('加载推荐视频失败：', err)
  }
}

// 页面挂载时加载数据
onMounted(() => {
  loadVideoDetail()
})


</script>

<template>
  <div class="video-detail-container" style="background-color: #f9f9f9; padding: 20px 0; min-height: 100vh;">
    <div class="container" style="max-width: 1200px; margin: 0 auto; padding: 0 20px;">
      <div class="main-wrapper" style="display: flex; gap: 20px;">
        <!-- 左侧主内容区（70%宽度） -->
        <div class="main-content" style="width: 70%;">
          <VideoPlayerWithDanmaku :current-video="currentVideo" />
          <VideoInfoBar :current-video="currentVideo" />
          <CommentSection />
        </div>

        <!-- 右侧推荐栏（30%宽度） -->
        <div class="sidebar" style="width: 30%;">
          <RecommendVideoSidebar :recommend-videos="recommendVideos" />
        </div>
      </div>
    </div>
  </div>

</template>

<style scoped>

</style>
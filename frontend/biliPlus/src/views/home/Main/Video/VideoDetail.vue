<!-- src/views/video/VideoDetail.vue -->
<template>
  <div class="video-detail-container">
    <!-- 返回按钮 -->
<!--    <el-button types="text" @click="handleBackClick">← 返回上一页</el-button>-->

    <!-- 顶部：标题 + 作者 -->
    <div class="header">
      <div class="title-section">
        <h1 class="video-title">{{ currentVideo.title }}</h1>
        <div class="video-meta">
          <span>{{ formatCount(currentVideo.playCount) }} 播放</span>
          <span>•</span>
          <span>{{ formatDate(currentVideo.publishTime) }}</span>
        </div>
      </div>

      <div class="author-info">
        <div class="author-row">
          <el-avatar :src="currentVideo.author.avatar" size="small" />
          <span class="author-name">{{ currentVideo.author.name }}</span>
        </div>
        <span class="fans-count">{{ formatCount(currentVideo.author.fansCount) }} 粉丝</span>
        <el-button type="primary" size="small" @click="goTomessage">
          私信
        </el-button>
        <el-button
            size="small"
            @click="handleFollow"
            :type="followed ? 'default' : 'primary'"
            :class="{ 'follow-success': followAnimating }"
        >
          {{ followed ? '已关注' : '+ 关注' }}
        </el-button>
      </div>
    </div>

    <!-- 主内容区 -->
    <el-row :gutter="24">
      <!-- 左侧：播放器 + 互动 + 评论 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="17">
        <!-- 弹幕播放器 -->
        <DanmakuPlayer
            :src="currentVideo.videoUrl"
            :poster="currentVideo.coverUrl || 'https://via.placeholder.com/1280x720/e0e0e0?text=封面加载中'"
            :video-id="videoId"
            class="danmaku-player"
        />

        <!-- 互动操作栏 -->
        <div class="footer-actions">
          <el-button
              type="text"
              @click="handleLike"
              :class="{ 'like-animate': likeAnimating, 'is-liked': liked }"
          >
            <el-icon><Opportunity /></el-icon>
            <span>{{ formatCount(likeCount) }}</span>
          </el-button>
          <el-button
              type="text"
              @click="handleCollect"
              :class="{ 'is-collected': collected }"
          >
            <el-icon><StarFilled /></el-icon>
            <span>{{ formatCount(collectCount) }}</span>
          </el-button>
          <el-button type="text" @click="handleShare">
            <el-icon><Share /></el-icon>
            <span>{{ formatCount(shareCount) }}</span>
          </el-button>
        </div>

        <!-- 评论区 -->
        <CommentSection :video-id="videoId" />
      </el-col>

      <!-- 右侧：推荐视频 -->
      <el-col :xs="0" :sm="0" :md="8" :lg="7">
        <RecommendSidebar :videos="recommendVideos" @video-click="handleRecommendClick" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref,watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 组件
import DanmakuPlayer from '@/views/home/Main/Video/components/DanmakuPlayer.vue'
import CommentSection from '@/views/home/Main/Video/comment/CommentSection.vue'
import RecommendSidebar from '@/views/home/Main/Video/components/RecommendSidebar.vue'

// API
import { getVideoDetail, getUserInfo, getRecommendVideos } from '@/api/video.js'
import { toggleLike, toggleFavorite, toggleFollow, getVideoInteractionStatus, getUserInteractionStatus } from '@/api/interaction'
import { useUserStore } from '@/store/user'

// 路由
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 私信
const goTomessage=()=>{
  const author = currentVideo.value.author
  // 检查author是否存在以及是否有authorId
  if (!author || !author.authorId) {
    ElMessage.error('作者信息未加载完成，请稍后再试')
    return
  }
  // 跳转并携带作者 ID 和基本信息（通过 query 或 state）
  router.push({
    path: '/chat',
    query: {
      toUserId: author.authorId,
      toUserName: author.name,
      toUserAvatar: author.avatar
    }
  })
  console.log('跳转到私信页面----author信息:',author)
}

// 响应式数据
const currentVideo = ref({
  id: '',
  title: '',
  videoUrl: '',
  coverUrl: '',
  playCount: 0,
  publishTime: '',
  author: {
    authorId: null,
    name: '',
    avatar: '',
    fansCount: 0
  }
})
const videoId = route.params.id


const recommendVideos = ref([])
const liked = ref(false)
const collected = ref(false)
const followed = ref(false)
const likeCount = ref(0)
const collectCount = ref(0)
const shareCount = ref(0)
const likeAnimating = ref(false)
const followAnimating = ref(false)

// 格式化函数
const formatCount = (num) => {
  if (num >= 10000) {
    const val = num / 10000;
    return val % 1 === 0 ? `${val}万` : `${val.toFixed(1)}万`;
  }
  return num.toString();
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 加载数据
const loadVideoDetail = async () => {
  const currentId = route.params.id
  if (!currentId) return
  // 切换视频时先重置互动态，避免残留上一视频的关注/点赞状态
  followed.value = false
  liked.value = false
  collected.value = false
  try {
    const videoRes = await getVideoDetail(currentId)
    if (videoRes.code !== 1 || !videoRes.data) throw new Error(videoRes.msg || '获取视频失败')
    const videoData = videoRes.data

    // 先写入视频信息，避免用户接口失败导致整页空白
    currentVideo.value = {
      id: videoData.id,
      title: videoData.title,
      videoUrl: videoData.videoUrl || '',
      coverUrl: videoData.coverUrl || '',
      playCount: videoData.viewCount || 0,
      publishTime: videoData.createTime,
      author: {
        authorId: videoData.userId,
        name: videoData.nickname || `用户${videoData.userId}`,
        avatar: videoData.avatar || `https://picsum.photos/100/100?random=${videoData.userId}`,
        fansCount: 0
      }
    }

    // 用户信息失败不影响播放
    try {
      if (videoData.userId) {
        const userRes = await getUserInfo(videoData.userId)
        if (userRes.code === 1 && userRes.data) {
          currentVideo.value.author.name = userRes.data.nickname || currentVideo.value.author.name
          currentVideo.value.author.avatar = userRes.data.avatar || currentVideo.value.author.avatar
          currentVideo.value.author.fansCount = userRes.data.fansCount || 0
          // 兜底：若视频详情缺 userId，用用户接口返回的 id
          if (!currentVideo.value.author.authorId && userRes.data.id) {
            currentVideo.value.author.authorId = userRes.data.id
          }
        }
      }
    } catch (e) {
      console.warn('获取作者信息失败', e)
    }

    // 并行加载互动状态（互不影响）
    await Promise.all([
      loadVideoInteraction(currentId),
      loadFollowStatus()
    ])
    loadRecommendVideos(currentId)
  } catch (err) {
    console.error('加载失败:', err)
    ElMessage.error(err.message || '加载视频失败')
  }
}

// 路由参数变化时重新加载（推荐列表切换视频）
watch(() => route.params.id, (id) => {
  if (id) loadVideoDetail()
})

// 视频点赞/收藏状态
const loadVideoInteraction = async (vid) => {
  try {
    const res = await getVideoInteractionStatus(vid)
    if (res.code === 1 && res.data) {
      liked.value = res.data.liked || false
      collected.value = res.data.collected || false
      likeCount.value = res.data.likeCount || 0
      collectCount.value = res.data.favoriteCount || 0
    }
  } catch (err) {
    console.error('加载视频互动状态失败:', err)
  }
}

// 关注状态（独立加载，避免被视频互动接口失败连带跳过）
const loadFollowStatus = async () => {
  const authorId = currentVideo.value.author?.authorId
  if (!userStore.userInfo?.id || !authorId) return
  // 自己不显示关注
  if (Number(userStore.userInfo.id) === Number(authorId)) {
    followed.value = false
    return
  }
  try {
    const followRes = await getUserInteractionStatus(Number(authorId))
    if (followRes.code === 1 && followRes.data) {
      followed.value = !!followRes.data.followed
      if (followRes.data.fansCount != null) {
        currentVideo.value.author.fansCount = followRes.data.fansCount
      }
    }
  } catch (err) {
    console.error('加载关注状态失败:', err)
  }
}

const loadRecommendVideos = async (id) => {
  try {
    const res = await getRecommendVideos(id)
    console.log('获取推荐视频结果:', res)
    if (res.code === 1) { // ✅
      recommendVideos.value = res.data.records.map(item => ({ // ✅ res.data 就是 { records: [...] }
        id: item.id,
        coverUrl: item.coverUrl || `https://picsum.photos/200/112?random=${item.id}`,
        title: item.title,
        author: `用户${item.userId}`,
        duration: item.duration || `${Math.floor(Math.random() * 30)}:${Math.floor(Math.random() * 60).toString().padStart(2, '0')}`,
        playCount: item.viewCount || 0,
        likeCount: item.likeCount || 0,
        tag: item.categoryId === 10 ? '原创' : '热门'
      }))
    }
  } catch (err) {
    console.error('加载推荐视频失败:', err)
  }
}
// 事件处理
const handleBackClick = () => {
  router.go(-1)
}

const handleRecommendClick = (item) => {
  router.push(`/video/${item.id}`)
}

const handleLike = async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await toggleLike(videoId)
    if (res.code === 1 && res.data) {
      liked.value = res.data.liked
      likeCount.value = res.data.likeCount

      // 点赞动画
      if (liked.value) {
        likeAnimating.value = true
        setTimeout(() => {
          likeAnimating.value = false
        }, 400)
      }

      ElMessage.success(liked.value ? '点赞成功！' : '已取消点赞')
    }
  } catch (err) {
    console.error('点赞失败:', err)
    ElMessage.error('操作失败')
  }
}

const handleCollect = async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await toggleFavorite(videoId)
    if (res.code === 1 && res.data) {
      collected.value = res.data.collected
      collectCount.value = res.data.favoriteCount
      ElMessage.success(collected.value ? '收藏成功！' : '已取消收藏')
    }
  } catch (err) {
    console.error('收藏失败:', err)
    ElMessage.error('操作失败')
  }
}

const handleShare = () => {
  ElMessage.info('分享功能暂未实现')
}

const handleFollow = async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const authorId = currentVideo.value.author?.authorId
  if (!authorId) {
    ElMessage.error('作者信息未加载')
    return
  }
  try {
    const res = await toggleFollow(authorId)
    if (res.code === 1 && res.data) {
      followed.value = res.data.followed
      currentVideo.value.author.fansCount = res.data.fansCount

      // 关注动画
      if (followed.value) {
        followAnimating.value = true
        setTimeout(() => {
          followAnimating.value = false
        }, 300)
      }

      ElMessage.success(followed.value ? '已关注该UP主' : '已取消关注')
    }
  } catch (err) {
    console.error('关注失败:', err)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadVideoDetail()
})
</script>

<style scoped>
.video-detail-container {
  background-color: var(--paper);
  padding: var(--space-lg) var(--space-xl);
  min-height: calc(100vh - 60px);
  max-width: 1400px;
  margin: 0 auto;
}

/* 顶部区域 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-lg);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--line);
}

.title-section {
  flex: 1;
  margin-right: var(--space-lg);
}

.video-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 var(--space-sm);
  color: var(--ink);
  line-height: 1.4;
  letter-spacing: -0.3px;
}

.video-meta {
  font-size: 13px;
  color: var(--mist);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.video-meta span {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.author-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-sm);
  min-width: 180px;
  font-size: 14px;
  color: var(--ink-secondary);
}

.author-row {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.author-name {
  font-weight: 600;
  color: var(--ink);
}

.fans-count {
  color: var(--mist);
  font-size: 12px;
}

.author-info .el-button {
  border-radius: var(--radius-md);
  font-weight: 500;
}

/* 主内容区 */
.el-row {
  margin: 0 calc(-1 * var(--space-md));
}

.el-col {
  padding: 0 var(--space-md);
}

/* 播放器 */
:deep(.dplayer-container) {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  background: #000;
  margin-bottom: var(--space-lg);
}

/* 互动栏 */
.footer-actions {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  padding: var(--space-md) 0;
  background: var(--paper-white);
  border-radius: var(--radius-md);
  padding: var(--space-md) var(--space-lg);
  margin-bottom: var(--space-lg);
  box-shadow: var(--shadow-sm);
}

.footer-actions .el-button {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  height: auto;
  line-height: 1.4;
  color: var(--ink-secondary);
  font-size: 14px;
  font-weight: 500;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.footer-actions .el-button:hover:not(:disabled) {
  color: var(--brand);
  background: var(--brand-bg);
}

.footer-actions .el-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.footer-actions .el-button:first-child:hover:not(:disabled) {
  color: var(--like);
  background: rgba(255, 107, 107, 0.1);
}

/* 点赞激活状态 */
.footer-actions .el-button.is-liked {
  color: var(--like);
}

/* 点赞动画 */
@keyframes likeHeart {
  0% { transform: scale(1); }
  25% { transform: scale(1.3); }
  50% { transform: scale(0.95); }
  75% { transform: scale(1.15); }
  100% { transform: scale(1); }
}

.footer-actions .el-button.like-animate .el-icon {
  animation: likeHeart 0.4s ease-in-out;
  color: var(--like);
}

/* 收藏激活状态 */
.footer-actions .el-button.is-collected {
  color: var(--warning);
}

.footer-actions .el-icon {
  font-size: 20px;
}

/* 响应式 */
@media (max-width: 992px) {
  .video-detail-container {
    padding: var(--space-md);
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }

  .author-info {
    align-items: flex-start;
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .title-section {
    margin-right: 0;
  }

  .video-title {
    font-size: 18px;
  }

  .footer-actions {
    gap: var(--space-md);
    padding: var(--space-md);
  }

  /* 隐藏右侧推荐栏 */
  .el-col:nth-child(2) {
    display: none;
  }
}
</style>

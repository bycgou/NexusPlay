<template>
  <div class="user-space">
    <div v-if="loading" class="loading">加载中...</div>
    <template v-else-if="profile">
      <div class="profile-card">
        <el-avatar :size="80" :src="profile.avatar || defaultAvatar" />
        <div class="info">
          <h2 class="name">{{ profile.nickname || profile.username || '用户' }}</h2>
          <p class="sig">{{ profile.signature || '这个人很懒，什么都没写' }}</p>
          <div class="stats">
            <span>{{ formatFans(profile.fansCount) }} 粉丝</span>
            <span>{{ formatFans(profile.followingCount) }} 关注</span>
          </div>
        </div>
        <div class="actions" v-if="!isSelf">
          <el-button
              type="primary"
              round
              :loading="followLoading"
              @click="handleFollow"
          >{{ followed ? '已关注' : '+ 关注' }}</el-button>
          <el-button round @click="goChat">私信</el-button>
        </div>
      </div>

      <h3 class="section-title">TA 的视频</h3>
      <VideoList :video-data="videoList" :loading="videoLoading">
        <template #title>投稿</template>
        <template #video-item="{ item }">
          <VideoItem :item="item" @click="goVideo(item)" />
        </template>
      </VideoList>
    </template>
    <el-empty v-else description="用户不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/api/user'
import { getVideoList } from '@/api/video'
import { toggleFollow, getUserInteractionStatus } from '@/api/interaction'
import { useUserStore } from '@/store/user'
import VideoList from '@/views/home/Main/Video/VideoList.vue'
import VideoItem from '@/views/home/Main/Video/VideoItem.vue'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const videoLoading = ref(false)
const followLoading = ref(false)
const followed = ref(false)
const profile = ref(null)
const videoList = ref([])
const userId = route.params.id

const isSelf = computed(() => String(userStore.userInfo?.id) === String(userId))

const ensureLogin = () => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const loadFollowStatus = async () => {
  if (isSelf.value || !userStore.userInfo?.id) return
  try {
    const res = await getUserInteractionStatus(Number(userId))
    if (res.code === 1 && res.data) {
      followed.value = !!res.data.followed
      if (profile.value) {
        profile.value.fansCount = res.data.fansCount ?? profile.value.fansCount
      }
    }
  } catch (e) {
    console.warn('获取关注状态失败', e)
  }
}

const handleFollow = async () => {
  if (!ensureLogin()) return
  followLoading.value = true
  try {
    const res = await toggleFollow(Number(userId))
    if (res.code === 1 && res.data) {
      followed.value = !!res.data.followed
      if (profile.value) {
        profile.value.fansCount = res.data.fansCount ?? profile.value.fansCount
      }
      ElMessage.success(followed.value ? '关注成功' : '已取消关注')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

const formatFans = (n) => {
  const num = Number(n) || 0
  if (num >= 10000) {
    const v = num / 10000
    return v % 1 === 0 ? `${v}万` : `${v.toFixed(1)}万`
  }
  return String(num)
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await getUserInfo(Number(userId))
    if (res.code === 1 && res.data) {
      profile.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 按 userId 精确拉取该 UP 的公开视频
const loadVideos = async () => {
  videoLoading.value = true
  try {
    const res = await getVideoList({ userId: Number(userId), page: 1, pageSize: 20 })
    if (res.code === 1 && Array.isArray(res.data?.records)) {
      videoList.value = res.data.records.map((item) => ({
        id: item.id,
        coverUrl: item.coverUrl,
        title: item.title,
        author: item.nickname || profile.value?.nickname || 'UP主',
        duration: item.duration || '00:00',
        playCount: item.viewCount || 0,
        likeCount: item.likeCount || 0,
        createTime: item.createTime,
      }))
    }
  } catch (e) {
    console.error(e)
  } finally {
    videoLoading.value = false
  }
}

const goVideo = (item) => {
  if (item?.id) router.push(`/video/${item.id}`)
}

const goChat = () => {
  if (!profile.value) return
  router.push({
    path: '/chat',
    query: {
      toUserId: profile.value.id,
      toUserName: profile.value.nickname || profile.value.username,
      toUserAvatar: profile.value.avatar,
    },
  })
}

onMounted(() => {
  loadProfile()
  loadVideos()
  loadFollowStatus()
})
</script>

<style scoped>
.user-space {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 16px 40px;
}

.loading {
  color: #94a3b8;
  padding: 40px 0;
  text-align: center;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #eef0f4;
  margin-bottom: 24px;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  margin: 0 0 6px;
  font-size: 20px;
  color: #18191c;
}

.sig {
  margin: 0 0 8px;
  color: #64748b;
  font-size: 13px;
}

.stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #94a3b8;
}

.actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.section-title {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
}
</style>

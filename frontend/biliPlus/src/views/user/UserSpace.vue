<template>
  <div class="user-space">
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="profile">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="avatar-wrap">
          <el-avatar :size="96" :src="profile.avatar || defaultAvatar" />
        </div>

        <div class="profile-main">
          <div class="name-row">
            <h2 class="name">{{ profile.nickname || profile.username || '用户' }}</h2>
            <span class="uid">UID: {{ profile.id }}</span>
            <el-tag v-if="isSelf" size="small" type="primary" effect="light">我自己</el-tag>
          </div>

          <p class="sig">{{ profile.signature || '这个人很懒，什么都没写' }}</p>
          <p v-if="profile.email && isSelf" class="email">邮箱：{{ profile.email }}</p>

          <div class="stats">
            <div class="stat-item" @click="activeTab = 'works'">
              <span class="num">{{ formatCount(workCount) }}</span>
              <span class="label">投稿</span>
            </div>
            <div class="stat-item">
              <span class="num">{{ formatCount(profile.fansCount) }}</span>
              <span class="label">粉丝</span>
            </div>
            <div class="stat-item">
              <span class="num">{{ formatCount(profile.followingCount) }}</span>
              <span class="label">关注</span>
            </div>
          </div>
        </div>

        <div class="actions">
          <template v-if="isSelf">
            <el-button type="primary" round @click="goSetting">
              <el-icon><EditPen /></el-icon>
              <span>编辑资料</span>
            </el-button>
            <el-button round @click="goContribute">
              <el-icon><VideoCamera /></el-icon>
              <span>去投稿</span>
            </el-button>
          </template>
          <template v-else>
            <el-button
                type="primary"
                round
                :loading="followLoading"
                @click="handleFollow"
            >{{ followed ? '已关注' : '+ 关注' }}</el-button>
            <el-button round @click="goChat">私信</el-button>
          </template>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="content-card">
        <el-tabs v-model="activeTab" class="space-tabs" @tab-change="handleTabChange">
          <el-tab-pane :label="`投稿 ${works.length ? `(${works.length})` : ''}`" name="works">
            <EmptyState
                v-if="!worksLoading && works.length === 0"
                icon="VideoCamera"
                :title="isSelf ? '还没有投稿' : 'TA 还没有投稿'"
                :description="isSelf ? '快去投稿你的第一个视频吧' : '等待 UP 主更新'"
                :action-text="isSelf ? '去投稿' : ''"
                @action="goContribute"
            />
            <VideoList v-else :video-data="works" :loading="worksLoading">
              <template #title>{{ isSelf ? '我的投稿' : 'TA 的视频' }}</template>
              <template #video-item="{ item }">
                <VideoItem :item="item" />
              </template>
            </VideoList>
          </el-tab-pane>

          <el-tab-pane v-if="isSelf" :label="`点赞 ${liked.length ? `(${liked.length})` : ''}`" name="liked">
            <EmptyState
                v-if="!likedLoading && liked.length === 0"
                icon="Star"
                title="还没有点赞视频"
                description="看到喜欢的视频点个赞吧"
            />
            <VideoList v-else :video-data="liked" :loading="likedLoading">
              <template #title>我点赞的视频</template>
              <template #video-item="{ item }">
                <VideoItem :item="item" />
              </template>
            </VideoList>
          </el-tab-pane>

          <el-tab-pane v-if="isSelf" :label="`收藏 ${favorited.length ? `(${favorited.length})` : ''}`" name="favorites">
            <EmptyState
                v-if="!favoritesLoading && favorited.length === 0"
                icon="Collection"
                title="还没有收藏视频"
                description="收藏感兴趣的视频，方便以后回看"
            />
            <VideoList v-else :video-data="favorited" :loading="favoritesLoading">
              <template #title>我的收藏</template>
              <template #video-item="{ item }">
                <VideoItem :item="item" />
              </template>
            </VideoList>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>

    <el-empty v-else description="用户不存在" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/api/user'
import { getVideoList } from '@/api/video'
import {
  toggleFollow,
  getUserInteractionStatus,
  getMyLikedVideos,
  getMyFavoriteVideos
} from '@/api/interaction'
import { useUserStore } from '@/store/user'
import VideoList from '@/views/home/Main/Video/VideoList.vue'
import VideoItem from '@/views/home/Main/Video/VideoItem.vue'
import EmptyState from '@/components/EmptyState.vue'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const worksLoading = ref(false)
const likedLoading = ref(false)
const favoritesLoading = ref(false)
const followLoading = ref(false)
const followed = ref(false)
const profile = ref(null)
const works = ref([])
const liked = ref([])
const favorited = ref([])
const activeTab = ref('works')

const userId = computed(() => route.params.id)
const isSelf = computed(() => String(userStore.userInfo?.id) === String(userId.value))
const workCount = computed(() => works.value.length)

const ensureLogin = () => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const formatCount = (n) => {
  const num = Number(n) || 0
  if (num >= 10000) {
    const v = num / 10000
    return v % 1 === 0 ? `${v}万` : `${v.toFixed(1)}万`
  }
  return String(num)
}

const mapVideoItem = (item, fallbackAuthor) => ({
  id: item.id,
  coverUrl: item.coverUrl,
  title: item.title,
  author: item.nickname || fallbackAuthor || 'UP主',
  duration: item.duration || '00:00',
  playCount: item.viewCount || 0,
  likeCount: item.likeCount || 0,
  createTime: item.createTime,
})

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await getUserInfo(Number(userId.value))
    if (res.code === 1 && res.data) {
      profile.value = res.data
      document.title = `${res.data.nickname || res.data.username || '用户空间'} - NexusPlay`
    } else {
      profile.value = null
    }
  } catch (e) {
    console.error(e)
    profile.value = null
  } finally {
    loading.value = false
  }
}

const loadWorks = async () => {
  worksLoading.value = true
  try {
    const res = await getVideoList({ userId: Number(userId.value), page: 1, pageSize: 50 })
    if (res.code === 1 && Array.isArray(res.data?.records)) {
      works.value = res.data.records.map((item) =>
          mapVideoItem(item, profile.value?.nickname)
      )
    }
  } catch (e) {
    console.error(e)
  } finally {
    worksLoading.value = false
  }
}

const loadLiked = async () => {
  if (!isSelf.value) return
  likedLoading.value = true
  try {
    const res = await getMyLikedVideos(1, 50)
    if (res.code === 1 && Array.isArray(res.data?.records)) {
      liked.value = res.data.records.map((item) =>
          mapVideoItem(item, profile.value?.nickname)
      )
    }
  } catch (e) {
    console.error(e)
  } finally {
    likedLoading.value = false
  }
}

const loadFavorites = async () => {
  if (!isSelf.value) return
  favoritesLoading.value = true
  try {
    const res = await getMyFavoriteVideos(1, 50)
    if (res.code === 1 && Array.isArray(res.data?.records)) {
      favorited.value = res.data.records.map((item) =>
          mapVideoItem(item, profile.value?.nickname)
      )
    }
  } catch (e) {
    console.error(e)
  } finally {
    favoritesLoading.value = false
  }
}

const loadFollowStatus = async () => {
  if (isSelf.value || !userStore.userInfo?.id) return
  try {
    const res = await getUserInteractionStatus(Number(userId.value))
    if (res.code === 1 && res.data) {
      followed.value = !!res.data.followed
      if (profile.value) {
        profile.value.fansCount = res.data.fansCount ?? profile.value.fansCount
        profile.value.followingCount = res.data.followingCount ?? profile.value.followingCount
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
    const res = await toggleFollow(Number(userId.value))
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

const goChat = () => {
  if (!ensureLogin() || !profile.value) return
  router.push({
    path: '/chat',
    query: {
      toUserId: profile.value.id,
      toUserName: profile.value.nickname || profile.value.username,
      toUserAvatar: profile.value.avatar,
    },
  })
}

const goSetting = () => router.push('/setting/profile')
const goContribute = () => router.push('/contribute')

const handleTabChange = (name) => {
  if (name === 'liked' && liked.value.length === 0 && !likedLoading.value) {
    loadLiked()
  }
  if (name === 'favorites' && favorited.value.length === 0 && !favoritesLoading.value) {
    loadFavorites()
  }
}

const loadAll = async () => {
  activeTab.value = 'works'
  followed.value = false
  liked.value = []
  favorited.value = []
  await loadProfile()
  if (!profile.value) return
  loadWorks()
  loadFollowStatus()
  if (isSelf.value) {
    loadLiked()
    loadFavorites()
  }
}

watch(
    () => route.params.id,
    (id, oldId) => {
      if (id && String(id) !== String(oldId)) {
        loadAll()
      }
    }
)

onMounted(loadAll)
</script>

<style scoped>
.user-space {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.loading-wrap {
  background: var(--paper-white, #fff);
  border-radius: 14px;
  padding: 32px;
}

.profile-card {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding: 28px 32px;
  background: linear-gradient(135deg, #fff7fb 0%, #f5f9ff 100%);
  border: 1px solid #eef0f4;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
}

.avatar-wrap {
  flex-shrink: 0;
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fb7299, #00aeec);
}

.avatar-wrap :deep(.el-avatar) {
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.name {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #18191c;
}

.uid {
  font-size: 13px;
  color: #94a3b8;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.sig {
  margin: 0 0 8px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}

.email {
  margin: 0 0 12px;
  color: #94a3b8;
  font-size: 12px;
}

.stats {
  display: flex;
  gap: 28px;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  cursor: default;
}

.stat-item:first-child {
  cursor: pointer;
}

.stat-item .num {
  font-size: 18px;
  font-weight: 700;
  color: #18191c;
  line-height: 1.2;
}

.stat-item .label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;
}

.actions .el-button {
  min-width: 120px;
}

.content-card {
  background: var(--paper-white, #fff);
  border: 1px solid #eef0f4;
  border-radius: 16px;
  padding: 8px 8px 16px;
  min-height: 320px;
}

.space-tabs :deep(.el-tabs__header) {
  margin: 0 16px 8px;
}

.space-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
}

.space-tabs :deep(.el-tabs__item.is-active) {
  color: var(--brand, #fb7299);
}

.space-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--brand, #fb7299);
}

@media (max-width: 768px) {
  .profile-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 24px 16px;
    gap: 16px;
  }

  .name-row {
    justify-content: center;
  }

  .stats {
    justify-content: center;
    width: 100%;
  }

  .actions {
    flex-direction: row;
    width: 100%;
  }

  .actions .el-button {
    flex: 1;
  }
}
</style>

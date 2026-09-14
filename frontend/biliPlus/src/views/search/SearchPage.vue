<template>
  <div class="search-page">
    <div class="search-header">
      <div class="title-row">
        <h2>搜索</h2>
        <span class="kw" v-if="keyword">「{{ keyword }}」</span>
      </div>
      <div class="search-bar">
        <el-input
            v-model="keyword"
            placeholder="搜索视频、用户..."
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="result-tabs">
      <el-tab-pane :label="`综合`" name="all">
        <div v-if="!keyword" class="empty-tip">输入关键词开始搜索</div>
        <template v-else>
          <section class="block" v-if="userList.length">
            <h3 class="block-title">相关用户</h3>
            <div class="user-grid">
              <div
                  v-for="u in userList"
                  :key="u.id"
                  class="user-card"
                  @click="goUser(u)"
              >
                <el-avatar :size="56" :src="u.avatar || defaultAvatar" />
                <div class="user-meta">
                  <div class="name">{{ u.nickname || u.username || '用户' }}</div>
                  <div class="sub">{{ formatFans(u.fansCount) }} 粉丝</div>
                </div>
              </div>
            </div>
          </section>

          <section class="block">
            <h3 class="block-title">相关视频</h3>
            <VideoList :video-data="videoList" :loading="videoLoading">
              <template #title>视频结果</template>
              <template #video-item="{ item }">
                <VideoItem :item="item" @click="goVideo(item)" />
              </template>
            </VideoList>
          </section>
        </template>
      </el-tab-pane>

      <el-tab-pane :label="`视频 ${videoList.length || ''}`" name="video">
        <VideoList :video-data="videoList" :loading="videoLoading">
          <template #title>视频结果</template>
          <template #video-item="{ item }">
            <VideoItem :item="item" @click="goVideo(item)" />
          </template>
        </VideoList>
      </el-tab-pane>

      <el-tab-pane :label="`用户 ${userList.length || ''}`" name="user">
        <div v-if="userLoading" class="empty-tip">加载中...</div>
        <el-empty v-else-if="!userList.length" description="未找到相关用户" />
        <div v-else class="user-grid">
          <div
              v-for="u in userList"
              :key="u.id"
              class="user-card"
              @click="goUser(u)"
          >
            <el-avatar :size="56" :src="u.avatar || defaultAvatar" />
            <div class="user-meta">
              <div class="name">{{ u.nickname || u.username || '用户' }}</div>
              <div class="sub">{{ u.signature || '这个人很懒，什么都没写' }}</div>
              <div class="sub">{{ formatFans(u.fansCount) }} 粉丝</div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getVideoList } from '@/api/video'
import { searchUsers } from '@/api/user'
import VideoList from '@/views/home/Main/Video/VideoList.vue'
import VideoItem from '@/views/home/Main/Video/VideoItem.vue'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.q || '')
const activeTab = ref('all')
const videoList = ref([])
const userList = ref([])
const videoLoading = ref(false)
const userLoading = ref(false)

const formatFans = (n) => {
  const num = Number(n) || 0
  if (num >= 10000) {
    const v = num / 10000
    return v % 1 === 0 ? `${v}万` : `${v.toFixed(1)}万`
  }
  return String(num)
}

const loadVideos = async (q) => {
  videoLoading.value = true
  try {
    const res = await getVideoList({ title: q, page: 1, pageSize: 20 })
    if (res.code === 1 && Array.isArray(res.data?.records)) {
      videoList.value = res.data.records.map((item) => ({
        id: item.id,
        coverUrl: item.coverUrl,
        title: item.title,
        author: item.nickname || 'UP主',
        duration: item.duration || '00:00',
        playCount: item.viewCount || 0,
        likeCount: item.likeCount || 0,
        createTime: item.createTime,
      }))
    } else {
      videoList.value = []
    }
  } catch (e) {
    console.error('搜索视频失败', e)
    videoList.value = []
  } finally {
    videoLoading.value = false
  }
}

const loadUsers = async (q) => {
  userLoading.value = true
  try {
    const res = await searchUsers(q, 20)
    if (res.code === 1 && Array.isArray(res.data)) {
      userList.value = res.data
    } else {
      userList.value = []
    }
  } catch (e) {
    console.error('搜索用户失败', e)
    userList.value = []
  } finally {
    userLoading.value = false
  }
}

const handleSearch = async () => {
  const q = (keyword.value || '').trim()
  if (!q) {
    videoList.value = []
    userList.value = []
    return
  }
  router.replace({ path: '/search', query: { q } })
  await Promise.all([loadVideos(q), loadUsers(q)])
}

const goVideo = (item) => {
  if (item?.id) router.push(`/video/${item.id}`)
}

const goUser = (u) => {
  if (u?.id) router.push(`/user/${u.id}`)
}

watch(
  () => route.query.q,
  (val) => {
    keyword.value = val || ''
    if (keyword.value) handleSearch()
  }
)

onMounted(() => {
  if (keyword.value) handleSearch()
})
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 16px 40px;
}

.search-header {
  margin-bottom: 12px;
}

.title-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.title-row h2 {
  margin: 0;
  font-size: 20px;
  color: #18191c;
}

.kw {
  color: #6c5ce7;
  font-size: 14px;
}

.search-bar {
  display: flex;
  gap: 10px;
  max-width: 560px;
}

.search-input {
  flex: 1;
}

.empty-tip {
  color: #94a3b8;
  font-size: 14px;
  padding: 24px 0;
}

.block {
  margin-bottom: 24px;
}

.block-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.user-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 280px;
  padding: 14px;
  background: #fff;
  border: 1px solid #eef0f4;
  border-radius: 12px;
  cursor: pointer;
  transition: box-shadow 0.15s ease, border-color 0.15s ease;
}

.user-card:hover {
  border-color: #6c5ce7;
  box-shadow: 0 4px 16px rgba(108, 92, 231, 0.12);
}

.user-meta {
  min-width: 0;
  flex: 1;
}

.name {
  font-size: 14px;
  font-weight: 600;
  color: #18191c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

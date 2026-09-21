<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
    deleteDynamic,
    getDynamicFeed,
    getHotDynamics,
    publishDynamic,
    toggleDynamicLike,
    type DynamicItem
} from '@/api/dynamic'

const router = useRouter()
const userStore = useUserStore()

const isLogin = computed(() => !!userStore.isLogin)
const myId = computed(() => Number(userStore.userInfo?.id) || 0)

// 已登录默认看关注流，未登录只能看广场
const activeTab = ref<'feed' | 'hot'>(isLogin.value ? 'feed' : 'hot')

const list = ref<DynamicItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

const composeText = ref('')
const publishing = ref(false)

const formatTime = (value?: string) => (value ? String(value).replace('T', ' ').slice(0, 16) : '')

const typeLabel = (type: number) => {
    if (type === 2) return '投稿视频'
    if (type === 3) return '转发'
    if (type === 4) return '开播'
    return '文字'
}

const isSelf = (item: DynamicItem) => myId.value > 0 && Number(item.userId) === myId.value

const emptyText = computed(() => {
    if (activeTab.value === 'feed') {
        return isLogin.value ? '关注的人还没有发布动态' : '登录后查看关注动态'
    }
    return '暂无动态'
})

const load = async () => {
    // 关注流需登录，未登录不请求
    if (activeTab.value === 'feed' && !isLogin.value) {
        list.value = []
        total.value = 0
        return
    }

    loading.value = true
    try {
        const req = activeTab.value === 'feed' ? getDynamicFeed : getHotDynamics
        const res: any = await req({ page: page.value, size })
        if (res?.code === 1) {
            list.value = res.data?.records || []
            total.value = Number(res.data?.total) || 0
        } else {
            list.value = []
            total.value = 0
            ElMessage.error(res?.msg || '加载动态失败')
        }
    } catch (e) {
        // 拦截器已统一提示，这里只重置列表
        list.value = []
        total.value = 0
    } finally {
        loading.value = false
    }
}

const handleTabChange = () => {
    page.value = 1
    load()
}

// ===== 发布 =====

const handlePublish = async () => {
    const content = composeText.value.trim()
    if (!content) {
        ElMessage.warning('请输入动态内容')
        return
    }
    if (content.length > 1000) {
        ElMessage.warning('动态内容不能超过1000字')
        return
    }

    publishing.value = true
    try {
        const res: any = await publishDynamic(content)
        if (res?.code === 1) {
            ElMessage.success('发布成功')
            composeText.value = ''
            // 发布后回到关注流首屏（自己发布的动态也在关注流里）
            activeTab.value = 'feed'
            page.value = 1
            await load()
        } else {
            ElMessage.error(res?.msg || '发布失败')
        }
    } catch (e) {
        // 拦截器已提示
    } finally {
        publishing.value = false
    }
}

// ===== 点赞 / 删除 =====

const handleLike = async (item: DynamicItem) => {
    if (!isLogin.value) {
        ElMessage.warning('请先登录后再点赞')
        return
    }
    try {
        const res: any = await toggleDynamicLike(item.id)
        if (res?.code === 1) {
            item.liked = !!res.data?.liked
            item.likeCount = Number(res.data?.likeCount) || 0
        } else {
            ElMessage.error(res?.msg || '点赞失败')
        }
    } catch (e) {
        // 拦截器已提示
    }
}

const handleDelete = async (item: DynamicItem) => {
    try {
        await ElMessageBox.confirm('删除后该动态将不再展示，确定删除吗？', '删除动态', {
            type: 'warning',
            confirmButtonText: '删除',
            cancelButtonText: '取消'
        })
    } catch {
        // 用户取消
        return
    }

    try {
        const res: any = await deleteDynamic(item.id)
        if (res?.code === 1) {
            ElMessage.success('动态已删除')
            // 当前页删空后回退上一页
            if (list.value.length === 1 && page.value > 1) page.value -= 1
            await load()
        } else {
            ElMessage.error(res?.msg || '删除失败')
        }
    } catch (e) {
        // 拦截器已提示
    }
}

// ===== 跳转 =====

const goUser = (userId?: number) => {
    if (userId) router.push(`/user/${userId}`)
}

// type=2 的投稿动态点卡片进视频页
const goVideo = (item: DynamicItem) => {
    if (item.type === 2 && item.videoId) {
        router.push(`/video/${item.videoId}`)
    }
}

const goLogin = () => {
    router.push('/login')
}

onMounted(() => {
    load()
})

// 登录态变化时刷新关注流可用性
watch(
    () => isLogin.value,
    (login) => {
        if (login && activeTab.value === 'feed') {
            page.value = 1
            load()
        } else if (!login && activeTab.value === 'feed') {
            list.value = []
            total.value = 0
        }
    }
)
</script>

<template>
  <div class="dynamic-feed">
    <div class="page-header">
      <div>
        <h2>动态</h2>
        <p class="sub">共 {{ total }} 条动态</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab" class="tabs" @tab-change="handleTabChange">
      <el-tab-pane label="关注" name="feed" />
      <el-tab-pane label="广场" name="hot" />
    </el-tabs>

    <!-- 发布框：仅登录可见 -->
    <div v-if="isLogin" class="compose">
      <el-input
          v-model="composeText"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="分享点什么吧..."
      />
      <div class="compose-actions">
        <el-button type="primary" :loading="publishing" @click="handlePublish">发布</el-button>
      </div>
    </div>
    <el-alert
        v-else
        class="login-tip"
        type="info"
        :closable="false"
        title="登录后可以发布动态、点赞并查看关注流"
        @click="goLogin"
    />

    <el-empty v-if="!loading && list.length === 0" :description="emptyText">
      <el-button v-if="!isLogin" type="primary" @click="goLogin">去登录</el-button>
      <el-button v-else @click="load">刷新</el-button>
    </el-empty>

    <div v-else v-loading="loading" class="list">
      <div
          v-for="item in list"
          :key="item.id"
          class="card"
          :class="{ clickable: item.type === 2 && !!item.videoId }"
          @click="goVideo(item)"
      >
        <div class="card-head">
          <el-avatar
              :size="40"
              :src="item.avatar || '/User.jpg'"
              class="avatar"
              @click.stop="goUser(item.userId)"
          />
          <div class="head-main">
            <div class="name-row">
              <span class="name" @click.stop="goUser(item.userId)">{{ item.nickname || '用户' }}</span>
              <el-tag size="small" effect="light" type="info">{{ typeLabel(item.type) }}</el-tag>
            </div>
            <span class="time">{{ formatTime(item.createTime) }}</span>
          </div>
        </div>

        <p v-if="item.content" class="content">{{ item.content }}</p>

        <!-- 投稿动态：展示关联视频 -->
        <div v-if="item.type === 2 && item.videoId" class="video-card">
          <img v-if="item.videoCoverUrl" :src="item.videoCoverUrl" class="video-cover" alt="" />
          <div v-else class="video-cover placeholder">
            <el-icon size="28"><VideoCameraFilled /></el-icon>
          </div>
          <div class="video-title">{{ item.videoTitle || '查看视频' }}</div>
        </div>

        <div class="card-foot">
          <span class="action like" :class="{ active: item.liked }" @click.stop="handleLike(item)">
            <el-icon size="16"><component :is="item.liked ? 'StarFilled' : 'Star'" /></el-icon>
            <span>{{ Number(item.likeCount) || 0 }}</span>
          </span>
          <span v-if="isSelf(item)" class="action delete" @click.stop="handleDelete(item)">
            <el-icon size="16"><Delete /></el-icon>
            <span>删除</span>
          </span>
        </div>
      </div>
    </div>

    <div v-if="total > size" class="pager">
      <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="(p: number) => { page = p; load() }"
      />
    </div>
  </div>
</template>

<style scoped>
.dynamic-feed {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: var(--ink);
}
.sub {
  margin: 4px 0 0;
  color: var(--mist);
  font-size: 13px;
}
.tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}
.compose {
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--paper-white);
  margin-bottom: 16px;
}
.compose-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
.login-tip {
  margin-bottom: 16px;
  cursor: pointer;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
}
.card {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--paper-white);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}
.card.clickable {
  cursor: pointer;
}
.card.clickable:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}
.card-head {
  display: flex;
  gap: 10px;
  align-items: center;
}
.avatar {
  cursor: pointer;
  flex-shrink: 0;
}
.head-main {
  flex: 1;
  min-width: 0;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  cursor: pointer;
}
.name:hover {
  color: var(--brand);
}
.time {
  font-size: 12px;
  color: var(--mist);
}
.content {
  margin: 10px 0 0;
  font-size: 14px;
  color: var(--ink-secondary);
  word-break: break-word;
  white-space: pre-wrap;
}
.video-card {
  margin-top: 10px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  overflow: hidden;
  max-width: 320px;
}
.video-cover {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: cover;
  display: block;
}
.video-cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--paper);
  color: var(--mist-light);
}
.video-title {
  padding: 8px 10px;
  font-size: 13px;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-foot {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 12px;
  font-size: 13px;
  color: var(--mist);
}
.action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color var(--transition-fast);
}
.action.like:hover,
.action.like.active {
  color: var(--like);
}
.action.delete:hover {
  color: var(--danger);
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

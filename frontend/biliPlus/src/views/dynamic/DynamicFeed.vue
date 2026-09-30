<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAuthPrompt } from '@/composables/useAuthPrompt'
import DynamicCard from '@/components/DynamicCard.vue'
import EmojiPicker from '@/views/components/EmojiPicker.vue'
import {
    deleteDynamic,
    getDynamicFeed,
    getHotDynamics,
    publishDynamic,
    toggleDynamicLike,
    type DynamicItem
} from '@/api/dynamic'

const router = useRouter()
const authPrompt = useAuthPrompt()
const userStore = useUserStore()

const isLogin = computed(() => !!userStore.isLogin)
const myId = computed(() => Number(userStore.userInfo?.id) || 0)

const activeTab = ref<'feed' | 'hot'>(isLogin.value ? 'feed' : 'hot')

const list = ref<DynamicItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

const composeText = ref('')
const publishing = ref(false)
const showEmoji = ref(false)
const textareaRef = ref()

const emptyText = computed(() => {
    if (activeTab.value === 'feed') {
        return isLogin.value ? '关注的人还没有发布动态' : '登录后查看关注动态'
    }
    return '广场上还没有动态'
})

const tabLabel = computed(() => (activeTab.value === 'feed' ? '关注' : '广场'))

const isSelf = (item: DynamicItem) => myId.value > 0 && Number(item.userId) === myId.value

const load = async (append = false) => {
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
            const records: DynamicItem[] = res.data?.records || []
            list.value = append ? [...list.value, ...records] : records
            total.value = Number(res.data?.total) || 0
        } else {
            if (!append) {
                list.value = []
                total.value = 0
            }
            ElMessage.error(res?.msg || '加载动态失败')
        }
    } catch {
        if (!append) {
            list.value = []
            total.value = 0
        }
    } finally {
        loading.value = false
    }
}

const handleTabChange = () => {
    page.value = 1
    load()
}

const canLoadMore = computed(() => list.value.length < total.value && !loading.value)

const loadMore = () => {
    if (!canLoadMore.value) return
    page.value += 1
    load(true)
}

// ===== 发布 =====

const pickEmoji = (emoji: string) => {
    composeText.value = (composeText.value || '') + emoji
    showEmoji.value = false
    textareaRef.value?.focus?.()
}

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
    if (!isLogin.value) {
        ElMessage.warning('请先登录后再发布')
        authPrompt.openLogin()
        return
    }

    publishing.value = true
    try {
        const res: any = await publishDynamic(content)
        if (res?.code === 1) {
            ElMessage.success('发布成功')
            composeText.value = ''
            showEmoji.value = false
            activeTab.value = 'feed'
            page.value = 1
            await load()
        } else {
            ElMessage.error(res?.msg || '发布失败')
        }
    } catch {
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
    } catch {
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
        return
    }

    try {
        const res: any = await deleteDynamic(item.id)
        if (res?.code === 1) {
            ElMessage.success('动态已删除')
            if (list.value.length === 1 && page.value > 1) page.value -= 1
            await load()
        } else {
            ElMessage.error(res?.msg || '删除失败')
        }
    } catch {
        // 拦截器已提示
    }
}

const handleShare = async () => {
    const url = `${window.location.origin}/dynamic`
    try {
        if (navigator.clipboard?.writeText) {
            await navigator.clipboard.writeText(url)
            ElMessage.success('动态页链接已复制')
            return
        }
    } catch {
        // fall through
    }
    ElMessage.info(url)
}

const goLogin = () => authPrompt.openLogin()
const goMe = () => router.push('/me')

onMounted(() => {
    load()
})

watch(
    () => isLogin.value,
    (login) => {
        if (login && activeTab.value === 'feed') {
            page.value = 1
            load()
        } else if (!login && activeTab.value === 'feed') {
            list.value = []
            total.value = 0
            activeTab.value = 'hot'
            page.value = 1
            load()
        }
    }
)
</script>

<template>
  <div class="dynamic-page">
    <div class="layout">
      <!-- 主栏 -->
      <main class="main-col">
        <div class="page-header">
          <div>
            <h2>动态</h2>
            <p class="sub">{{ tabLabel }} · 共 {{ total }} 条</p>
          </div>
          <el-button text @click="load()">刷新</el-button>
        </div>

        <el-tabs v-model="activeTab" class="tabs" @tab-change="handleTabChange">
          <el-tab-pane label="关注" name="feed" />
          <el-tab-pane label="广场" name="hot" />
        </el-tabs>

        <!-- 发布框 -->
        <div v-if="isLogin" class="compose">
          <div class="compose-row">
            <el-avatar :size="40" :src="userStore.userInfo?.avatar || '/User.jpg'" class="compose-avatar" @click="goMe" />
            <div class="compose-body">
              <el-input
                  ref="textareaRef"
                  v-model="composeText"
                  type="textarea"
                  :rows="3"
                  maxlength="1000"
                  show-word-limit
                  placeholder="分享点什么吧..."
                  @focus="showEmoji = false"
              />
              <div class="compose-actions">
                <div class="left-tools">
                  <el-button text class="emoji-btn" @click="showEmoji = !showEmoji">
                    <span class="emoji-glyph">😊</span>
                    <span>表情</span>
                  </el-button>
                  <span class="tip">文明发言，理性互动</span>
                </div>
                <el-button type="primary" :loading="publishing" @click="handlePublish">发布</el-button>
              </div>
              <EmojiPicker :visible="showEmoji" class="emoji-panel" @pick="pickEmoji" />
            </div>
          </div>
        </div>
        <div v-else class="login-card" @click="goLogin">
          <div class="login-main">
            <strong>登录后发布动态、点赞，并查看关注流</strong>
            <p>和 UP 主们保持同步，第一时间看到更新</p>
          </div>
          <el-button type="primary" round>去登录</el-button>
        </div>

        <!-- 列表 -->
        <EmptyState
            v-if="!loading && list.length === 0"
            icon="Promotion"
            title="暂无动态"
            :description="emptyText"
            :action-text="isLogin ? '去发布' : '去登录'"
            @action="isLogin ? (composeText = '') : goLogin()"
        />

        <div v-else class="list" v-loading="loading">
          <DynamicCard
              v-for="item in list"
              :key="item.id"
              :item="item"
              :show-delete="isSelf(item)"
              @like="handleLike"
              @delete="handleDelete"
          />

          <div v-if="list.length > 0" class="more-row">
            <el-button v-if="canLoadMore" :loading="loading" @click="loadMore">加载更多</el-button>
            <span v-else-if="!loading" class="end-text">已经到底啦</span>
          </div>
        </div>
      </main>

      <!-- 侧栏 -->
      <aside class="side-col">
        <section class="side-card">
          <h3 class="side-title">{{ isLogin ? '我的动态' : '动态广场' }}</h3>
          <template v-if="isLogin">
            <div class="side-user">
              <el-avatar :size="48" :src="userStore.userInfo?.avatar || '/User.jpg'" />
              <div>
                <div class="side-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '我' }}</div>
                <div class="side-meta">UID {{ myId }}</div>
              </div>
            </div>
            <div class="side-actions">
              <el-button size="small" @click="goMe">个人主页</el-button>
              <el-button size="small" type="primary" plain @click="handleShare">分享动态页</el-button>
            </div>
          </template>
          <template v-else>
            <p class="side-desc">浏览全站动态，登录后可以发布、点赞并关注感兴趣的人。</p>
            <el-button type="primary" size="small" @click="goLogin">立即登录</el-button>
          </template>
        </section>

        <section class="side-card tips">
          <h3 class="side-title">小提示</h3>
          <ul class="tip-list">
            <li>关注的人发新动态会出现在「关注」里</li>
            <li>稿件审核通过会自动同步为投稿动态</li>
            <li>开播时也会推送开播动态</li>
          </ul>
        </section>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.dynamic-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 20px;
  align-items: start;
}
.main-col {
  min-width: 0;
}
.side-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: sticky;
  top: 84px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
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
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--paper-white);
  margin-bottom: 16px;
}
.compose-row {
  display: flex;
  gap: 12px;
}
.compose-avatar {
  cursor: pointer;
  flex-shrink: 0;
}
.compose-body {
  flex: 1;
  min-width: 0;
  position: relative;
}
.compose-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.left-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}
.emoji-btn {
  color: var(--mist);
}
.emoji-glyph {
  font-size: 16px;
  line-height: 1;
}
.tip {
  font-size: 12px;
  color: var(--mist-light);
}
.emoji-panel {
  position: absolute;
  left: 0;
  bottom: 42px;
  z-index: 20;
  background: var(--paper-white);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-md);
  max-width: 320px;
  width: 100%;
}

.login-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--paper-white);
  margin-bottom: 16px;
  cursor: pointer;
}
.login-main strong {
  display: block;
  color: var(--ink);
  font-size: 14px;
}
.login-main p {
  margin: 6px 0 0;
  color: var(--mist);
  font-size: 12px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
}
.more-row {
  display: flex;
  justify-content: center;
  padding: 8px 0 4px;
}
.end-text {
  font-size: 12px;
  color: var(--mist-light);
}

.side-card {
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--paper-white);
}
.side-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
}
.side-user {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}
.side-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
}
.side-meta {
  font-size: 12px;
  color: var(--mist);
  margin-top: 2px;
}
.side-actions {
  display: flex;
  gap: 8px;
}
.side-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--ink-secondary);
  line-height: 1.6;
}
.tip-list {
  margin: 0;
  padding-left: 18px;
  color: var(--ink-secondary);
  font-size: 12px;
  line-height: 1.8;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .side-col {
    display: none;
  }
}
</style>

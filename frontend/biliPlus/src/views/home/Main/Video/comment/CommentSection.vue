<template>
  <div class="comment-section">
    <div class="comment-header">
      <span class="title">评论</span>
      <span class="count">{{ rootCount }}</span>
    </div>

    <!-- 顶部主输入框：一行 -->
    <div class="main-input">
      <img :src="myAvatar" class="avatar" alt="" />
      <div class="input-wrap">
        <input
            ref="mainInputRef"
            v-model="mainContent"
            class="line-input"
            type="text"
            :placeholder="mainPlaceholder"
            maxlength="500"
            @keyup.enter="sendMain"
        />
        <div class="input-bar">
          <button type="button" class="emoji-btn" @click="mainEmoji = !mainEmoji">😊</button>
          <span class="len">{{ mainContent.length }}/500</span>
          <button
              type="button"
              class="send-btn"
              :disabled="!mainContent.trim() || sendingMain"
              @click="sendMain"
          >{{ sendingMain ? '...' : '发表' }}</button>
        </div>
        <EmojiPicker v-if="mainEmoji" :visible="true" @pick="(e) => insertAt(mainContent, e, 'main')" />
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="loading" class="loading">加载中...</div>
    <el-empty v-else-if="!comments.length" description="还没有评论" :image-size="72" />

    <div v-else class="list">
      <div v-for="c in comments" :key="c.id" class="item">
        <img :src="c.avatar || defaultAvatar" class="avatar" alt="" />
        <div class="body">
          <div class="name">{{ c.nickname || '匿名用户' }}</div>
          <div class="text">{{ c.content }}</div>
          <div class="meta">
            <span class="time">{{ formatDate(c.createTime) }}</span>
            <button type="button" class="link" :class="{ on: c.liked }" @click="toggleLike(c)">
              {{ c.liked ? '已赞' : '赞' }} {{ c.likeCount || 0 }}
            </button>
            <button type="button" class="link" @click="openReply(c)">回复</button>
          </div>

          <!-- 回复输入框：挂在当前评论下方 -->
          <div v-if="replyTarget?.id === c.id" class="reply-editor">
            <div class="reply-input-wrap">
              <input
                  ref="replyInputRef"
                  v-model="replyContent"
                  class="line-input"
                  type="text"
                  :placeholder="`回复 @${c.nickname || '用户'}`"
                  maxlength="500"
                  @keyup.enter="sendReply"
                  @keyup.esc="closeReply"
              />
              <div class="input-bar">
                <button type="button" class="emoji-btn" @click="replyEmoji = !replyEmoji">😊</button>
                <button type="button" class="link" @click="closeReply">取消</button>
                <button
                    type="button"
                    class="send-btn"
                    :disabled="!replyContent.trim() || sendingReply"
                    @click="sendReply"
                >{{ sendingReply ? '...' : '回复' }}</button>
              </div>
              <EmojiPicker
                  v-if="replyEmoji"
                  :visible="true"
                  @pick="onPickReplyEmoji"
              />
            </div>
          </div>

          <!-- 子回复 -->
          <div v-if="c.children?.length" class="children">
            <div v-for="child in c.children" :key="child.id" class="child">
              <img :src="child.avatar || defaultAvatar" class="child-avatar" alt="" />
              <div>
                <div class="name">{{ child.nickname || '匿名用户' }}</div>
                <div class="text">{{ child.content }}</div>
                <div class="meta">
                  <span class="time">{{ formatDate(child.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import EmojiPicker from '@/views/components/EmojiPicker.vue'
import { postComment, getComments, toggleCommentLike } from '@/api/comment.js'
import { useUserStore } from '@/store/user.js'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const userStore = useUserStore()

const props = defineProps({
  videoId: { type: [String, Number], required: true },
})

const mainInputRef = ref(null)
const replyInputRef = ref(null)
const mainContent = ref('')
const replyContent = ref('')
const mainEmoji = ref(false)
const replyEmoji = ref(false)
const comments = ref([])
const loading = ref(false)
const error = ref('')
const sendingMain = ref(false)
const sendingReply = ref(false)
const replyTarget = ref(null)

const rootCount = computed(() => comments.value.length)
const myAvatar = computed(() => userStore.userInfo?.avatar || defaultAvatar)
const mainPlaceholder = computed(() =>
  userStore.userInfo?.id ? '说点什么...' : '登录后参与评论'
)

const organize = (list) => {
  const map = new Map()
  const roots = []
  list.forEach((item) => map.set(item.id, { ...item, children: [] }))
  list.forEach((item) => {
    if (item.parentId && item.parentId > 0 && map.has(item.parentId)) {
      map.get(item.parentId).children.push(map.get(item.id))
    } else {
      roots.push(map.get(item.id))
    }
  })
  return roots
}

const loadComments = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await getComments(props.videoId)
    if (res.code === 1) {
      comments.value = organize(res.data?.records || [])
    } else {
      throw new Error(res.msg || '获取评论失败')
    }
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const ensureLogin = () => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return false
  }
  return true
}

const insertAt = (targetRef, emoji, which) => {
  // 简化：直接追加
  if (which === 'main') {
    mainContent.value += emoji
    mainEmoji.value = false
  } else {
    replyContent.value += emoji
  }
}

const onPickReplyEmoji = (emoji) => {
  replyContent.value += emoji
  replyEmoji.value = false
}

const sendMain = async () => {
  if (!ensureLogin()) return
  const content = mainContent.value.trim()
  if (!content) return
  sendingMain.value = true
  try {
    const res = await postComment(props.videoId, content, 0)
    if (res.code === 1) {
      comments.value.unshift({
        id: res.data?.id || Date.now(),
        parentId: 0,
        nickname: userStore.userInfo.nickname || '我',
        avatar: userStore.userInfo.avatar,
        content,
        likeCount: 0,
        children: [],
        createTime: new Date().toISOString(),
      })
      mainContent.value = ''
      mainEmoji.value = false
      ElMessage.success('评论成功')
    } else {
      throw new Error(res.msg || '评论失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '评论失败')
  } finally {
    sendingMain.value = false
  }
}

const openReply = async (c) => {
  if (!ensureLogin()) return
  replyTarget.value = c
  replyContent.value = ''
  replyEmoji.value = false
  await nextTick()
  replyInputRef.value?.focus?.()
}

const closeReply = () => {
  replyTarget.value = null
  replyContent.value = ''
  replyEmoji.value = false
}

const sendReply = async () => {
  if (!ensureLogin() || !replyTarget.value) return
  const content = replyContent.value.trim()
  if (!content) return
  sendingReply.value = true
  const parent = replyTarget.value
  try {
    const res = await postComment(props.videoId, content, parent.id)
    if (res.code === 1) {
      parent.children = parent.children || []
      parent.children.push({
        id: res.data?.id || Date.now(),
        parentId: parent.id,
        nickname: userStore.userInfo.nickname || '我',
        avatar: userStore.userInfo.avatar,
        content,
        likeCount: 0,
        createTime: new Date().toISOString(),
      })
      closeReply()
      ElMessage.success('回复成功')
    } else {
      throw new Error(res.msg || '回复失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '回复失败')
  } finally {
    sendingReply.value = false
  }
}

const toggleLike = async (c) => {
  if (!ensureLogin()) return
  try {
    const res = await toggleCommentLike(c.id)
    if (res.code === 1 && res.data) {
      c.liked = res.data.liked
      c.likeCount = res.data.likeCount
    }
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const formatDate = (s) => {
  if (!s) return ''
  const d = new Date(s)
  return d.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

watch(
  () => props.videoId,
  (id) => {
    if (id) {
      closeReply()
      loadComments()
    }
  }
)

onMounted(() => {
  if (props.videoId) loadComments()
})
</script>

<style scoped>
.comment-section {
  margin-top: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px 20px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.06);
}

.comment-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 14px;
}

.title {
  font-size: 16px;
  font-weight: 700;
  color: #18191c;
}

.count {
  font-size: 13px;
  color: #94a3b8;
}

.main-input {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  background: #f1f5f9;
}

.child-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.input-wrap {
  flex: 1;
  min-width: 0;
  position: relative;
}

.line-input {
  width: 100%;
  height: 40px;
  border: 1px solid #e3e5e7;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  background: #f1f2f3;
  box-sizing: border-box;
  color: #18191c;
}

.line-input:focus {
  border-color: #6c5ce7;
  background: #fff;
}

.input-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.emoji-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
}

.len {
  font-size: 12px;
  color: #94a3b8;
}

.send-btn {
  border: none;
  height: 32px;
  padding: 0 16px;
  border-radius: 16px;
  background: #6c5ce7;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.send-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.error {
  color: #ef4444;
  font-size: 13px;
  margin-bottom: 8px;
}

.loading {
  color: #94a3b8;
  font-size: 13px;
  padding: 8px 0;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.item {
  display: flex;
  gap: 12px;
}

.body {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 13px;
  color: #61666d;
  font-weight: 600;
  margin-bottom: 4px;
}

.text {
  font-size: 14px;
  line-height: 1.6;
  color: #18191c;
  word-break: break-word;
  white-space: pre-wrap;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.link {
  border: none;
  background: transparent;
  color: #61666d;
  cursor: pointer;
  padding: 0;
  font-size: 12px;
}

.link:hover,
.link.on {
  color: #6c5ce7;
}

.reply-editor {
  margin-top: 10px;
  padding: 10px;
  background: #f7f8fa;
  border-radius: 8px;
}

.reply-input-wrap {
  position: relative;
}

.children {
  margin-top: 10px;
  padding: 10px 12px;
  background: #f7f8fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.child {
  display: flex;
  gap: 8px;
}
</style>

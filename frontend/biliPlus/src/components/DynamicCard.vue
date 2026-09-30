<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { DynamicItem } from '@/api/dynamic'

const props = defineProps<{
  item: DynamicItem
  /** 是否展示删除（默认由父组件判断 isSelf 后传入） */
  showDelete?: boolean
  /** 紧凑模式：个人空间嵌套时用 */
  compact?: boolean
}>()

const emit = defineEmits<{
  (e: 'like', item: DynamicItem): void
  (e: 'delete', item: DynamicItem): void
}>()

const router = useRouter()

const typeMeta = computed(() => {
  const t = Number(props.item.type)
  if (t === 2) return { label: '投稿视频', icon: 'VideoCameraFilled', tone: 'video' as const }
  if (t === 3) return { label: '转发', icon: 'Promotion', tone: 'repost' as const }
  if (t === 4) return { label: '开播', icon: 'VideoCamera', tone: 'live' as const }
  return { label: '文字', icon: 'EditPen', tone: 'text' as const }
})

const formatTime = (value?: string) => (value ? String(value).replace('T', ' ').slice(0, 16) : '')

const goUser = () => {
  if (props.item.userId) router.push(`/user/${props.item.userId}`)
}

const goVideo = () => {
  if (props.item.videoId) router.push(`/video/${props.item.videoId}`)
}

const goLive = () => {
  if (props.item.liveRoomId) router.push(`/live/room/${props.item.liveRoomId}`)
}

const handleBodyClick = () => {
  if (Number(props.item.type) === 2 && props.item.videoId) goVideo()
  else if (Number(props.item.type) === 4 && props.item.liveRoomId) goLive()
}
</script>

<template>
  <article
      class="dyn-card"
      :class="{ compact, clickable: (item.type === 2 && item.videoId) || (item.type === 4 && item.liveRoomId) }"
      @click="handleBodyClick"
  >
    <div class="card-head">
      <el-avatar
          :size="compact ? 36 : 44"
          :src="item.avatar || '/User.jpg'"
          class="avatar"
          @click.stop="goUser"
      />
      <div class="head-main">
        <div class="name-row">
          <span class="name" @click.stop="goUser">{{ item.nickname || '用户' }}</span>
          <span class="type-pill" :class="typeMeta.tone">
            <el-icon size="12"><component :is="typeMeta.icon" /></el-icon>
            {{ typeMeta.label }}
          </span>
          <span v-if="item.type === 4" class="live-dot" aria-hidden="true"></span>
        </div>
        <span class="time">{{ formatTime(item.createTime) }}</span>
      </div>
    </div>

    <!-- 文字 / 转发说明 -->
    <p v-if="item.content" class="content" :class="{ 'as-title': item.type === 2 }">
      {{ item.type === 2 && !item.videoTitle ? item.content : item.content }}
    </p>

    <!-- 投稿视频卡片 -->
    <div v-if="item.type === 2 && item.videoId" class="biz-card video-biz">
      <div class="cover-wrap">
        <img v-if="item.videoCoverUrl" :src="item.videoCoverUrl" class="cover" :alt="item.videoTitle || '视频封面'" />
        <div v-else class="cover placeholder">
          <el-icon size="28"><VideoCameraFilled /></el-icon>
        </div>
        <span class="play-mask">
          <el-icon size="22"><CaretRight /></el-icon>
        </span>
      </div>
      <div class="biz-meta">
        <div class="biz-title">{{ item.videoTitle || '查看视频' }}</div>
        <div class="biz-sub">点击进入播放页</div>
      </div>
    </div>

    <!-- 开播卡片 -->
    <div v-else-if="item.type === 4 && item.liveRoomId" class="biz-card live-biz">
      <div class="live-banner">
        <span class="live-tag">LIVE</span>
        <span class="live-text">{{ item.content || '主播开播了，快来看看' }}</span>
      </div>
      <div class="biz-sub">进入直播间</div>
    </div>

    <!-- 转发占位（后端暂未下发原动态摘要） -->
    <div v-else-if="item.type === 3" class="biz-card repost-biz">
      <div class="biz-sub">转发了一条动态</div>
    </div>

    <div class="card-foot">
      <span class="action like" :class="{ active: item.liked }" @click.stop="emit('like', item)">
        <el-icon size="16"><component :is="item.liked ? 'StarFilled' : 'Star'" /></el-icon>
        <span>{{ Number(item.likeCount) || 0 }}</span>
      </span>
      <span
          v-if="item.type === 2 && item.videoId"
          class="action"
          @click.stop="goVideo"
      >
        <el-icon size="16"><VideoPlay /></el-icon>
        <span>看视频</span>
      </span>
      <span
          v-else-if="item.type === 4 && item.liveRoomId"
          class="action live-action"
          @click.stop="goLive"
      >
        <el-icon size="16"><VideoCamera /></el-icon>
        <span>进直播间</span>
      </span>
      <span
          v-if="showDelete"
          class="action delete"
          @click.stop="emit('delete', item)"
      >
        <el-icon size="16"><Delete /></el-icon>
        <span>删除</span>
      </span>
    </div>
  </article>
</template>

<style scoped>
.dyn-card {
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--paper-white);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}
.dyn-card.clickable {
  cursor: pointer;
}
.dyn-card.clickable:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}
.dyn-card.compact {
  padding: 12px;
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
  flex-wrap: wrap;
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
.type-pill {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  line-height: 18px;
  border: 1px solid var(--line);
  color: var(--mist);
  background: var(--paper);
}
.type-pill.video {
  color: var(--brand);
  border-color: color-mix(in srgb, var(--brand) 35%, var(--line));
  background: var(--brand-bg);
}
.type-pill.live {
  color: var(--danger);
  border-color: color-mix(in srgb, var(--danger) 35%, var(--line));
  background: color-mix(in srgb, var(--danger) 8%, transparent);
}
.type-pill.repost {
  color: var(--success);
  border-color: color-mix(in srgb, var(--success) 35%, var(--line));
  background: color-mix(in srgb, var(--success) 8%, transparent);
}
.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--danger);
  box-shadow: 0 0 0 0 color-mix(in srgb, var(--danger) 55%, transparent);
  animation: pulse-dot 1.6s ease-out infinite;
}
@keyframes pulse-dot {
  0% { box-shadow: 0 0 0 0 color-mix(in srgb, var(--danger) 55%, transparent); }
  70% { box-shadow: 0 0 0 8px transparent; }
  100% { box-shadow: 0 0 0 0 transparent; }
}
.time {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: var(--mist);
}

.content {
  margin: 10px 0 0;
  font-size: 14px;
  color: var(--ink-secondary);
  word-break: break-word;
  white-space: pre-wrap;
  line-height: 1.6;
}
.content.as-title {
  font-weight: 600;
  color: var(--ink);
}

.biz-card {
  margin-top: 10px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  overflow: hidden;
  max-width: 360px;
  background: var(--paper);
}
.video-biz {
  display: flex;
  gap: 10px;
  padding: 8px;
  align-items: center;
}
.cover-wrap {
  position: relative;
  width: 148px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
}
.cover {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: cover;
  display: block;
}
.cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--line-light);
  color: var(--mist-light);
}
.play-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(0, 0, 0, 0.28);
  opacity: 0;
  transition: opacity var(--transition-fast);
}
.dyn-card.clickable:hover .play-mask {
  opacity: 1;
}
.biz-meta {
  min-width: 0;
  flex: 1;
}
.biz-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.biz-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--mist);
}

.live-biz {
  padding: 12px;
}
.live-banner {
  display: flex;
  align-items: center;
  gap: 8px;
}
.live-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #fff;
  background: var(--danger);
}
.live-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
}

.repost-biz {
  padding: 12px;
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
.action:hover {
  color: var(--brand);
}
.action.like:hover,
.action.like.active {
  color: var(--like);
}
.action.live-action:hover {
  color: var(--danger);
}
.action.delete:hover {
  color: var(--danger);
}

@media (max-width: 640px) {
  .cover-wrap {
    width: 120px;
  }
  .biz-card {
    max-width: 100%;
  }
}
</style>

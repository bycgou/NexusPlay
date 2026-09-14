<template>
  <div class="video-col">
    <div class="video-card" @click="handleClick">
      <!-- 视频封面 -->
      <div class="video-cover">
        <el-image
            :src="props.item.coverUrl"
            fit="cover"
            class="cover-img"
            loading="lazy"
        >
          <template #error>
            <div class="cover-placeholder">
              <el-icon size="32"><VideoCamera /></el-icon>
            </div>
          </template>
        </el-image>
        <el-tag
            v-if="props.item.tag"
            class="video-tag"
            :type="props.item.tag === '原创' ? 'success' : 'warning'"
            size="small"
            effect="dark"
        >
          {{ props.item.tag }}
        </el-tag>
        <div class="video-duration">{{ props.item.duration }}</div>
        <!-- 悬浮遮罩 -->
        <div class="cover-overlay">
          <el-icon size="40" class="play-icon"><VideoPlay /></el-icon>
        </div>
      </div>

      <!-- 视频信息 -->
      <div class="video-info">
        <h4 class="video-title">{{ props.item.title }}</h4>
        <div class="video-meta">
          <span class="video-author">
            <el-icon size="12"><User /></el-icon>
            {{ props.item.author }}
          </span>
          <span class="video-createtime">{{ formatDate(props.item.createTime) }}</span>
        </div>
        <div class="video-stats">
          <span class="stat-item">
            <el-icon size="12"><VideoPlay /></el-icon>
            {{ formatCount(props.item.playCount) }}
          </span>
          <span class="stat-item">
            <el-icon size="12"><Star /></el-icon>
            {{ formatCount(props.item.likeCount) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const props = defineProps({ item: Object })

const handleClick = () => {
  router.push(`/video/${props.item.id}`)
}

const formatCount = (num) => {
  if (num >= 10000) {
    const val = num / 10000;
    return val % 1 === 0 ? `${val}万` : `${val.toFixed(1)}万`;
  }
  return num.toString();
}

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${month}月${day}日`;
}
</script>

<style scoped>
.video-col {
  width: 20%;
  padding: 0 var(--space-sm) var(--space-lg);
  box-sizing: border-box;
}

.video-card {
  width: 100%;
  cursor: pointer;
  background: var(--paper-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-sm);
}

.video-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-hover);
}

.video-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16/9;
  overflow: hidden;
  background: var(--line-light);
}

.cover-img {
  width: 100%;
  height: 100%;
  transition: transform var(--transition-slow);
}

.video-card:hover .cover-img {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--line-light);
  color: var(--mist-light);
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.video-card:hover .cover-overlay {
  opacity: 1;
}

.play-icon {
  color: white;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.3));
}

.video-tag {
  position: absolute;
  top: var(--space-sm);
  left: var(--space-sm);
  font-size: 11px;
  border-radius: var(--radius-sm);
}

.video-duration {
  position: absolute;
  bottom: var(--space-sm);
  right: var(--space-sm);
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  font-size: 12px;
  font-family: var(--font-mono);
  padding: 2px var(--space-sm);
  border-radius: var(--radius-sm);
  backdrop-filter: blur(4px);
}

.video-info {
  padding: var(--space-md);
}

.video-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 var(--space-sm);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  min-height: 39px;
  transition: color var(--transition-fast);
}

.video-card:hover .video-title {
  color: var(--brand);
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xs);
  font-size: 12px;
  color: var(--mist);
}

.video-author {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 60%;
}

.video-stats {
  display: flex;
  gap: var(--space-md);
  font-size: 12px;
  color: var(--mist);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

/* 响应式 */
@media (max-width: 1200px) {
  .video-col {
    width: 25%;
  }
}

@media (max-width: 768px) {
  .video-col {
    width: 50%;
  }
}

@media (max-width: 480px) {
  .video-col {
    width: 100%;
  }
}
</style>
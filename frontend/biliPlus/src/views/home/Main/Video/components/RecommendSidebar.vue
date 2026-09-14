<template>
  <div class="recommend-sidebar">
    <h3 class="title">推荐视频</h3>
    <div class="video-list">
      <div
          v-for="item in videos"
          :key="item.id"
          class="video-item"
          @click="onVideoClick(item)"
      >
        <!-- 封面图 -->
        <div class="cover-container">
          <img
              :src="item.coverUrl"
              :alt="item.title"
              class="cover"
              @error="onImageError"
          />
          <span class="duration">{{ item.duration }}</span>
        </div>

        <!-- 标题与信息 -->
        <div class="info">
          <p class="title">{{ item.title }}</p>
          <p class="author">{{ item.author }}</p>
          <div class="stats">
            <span class="play-count">{{ formatCount(item.playCount) }} 播放</span>
            <span class="like-count">{{ formatCount(item.likeCount) }} 喜欢</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  videos: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['video-click'])

const onVideoClick = (item) => {
  emit('video-click', item)
}

// 数字格式化
const formatCount = (num) => {
  if (num >= 10000) {
    const val = num / 10000;
    return val % 1 === 0 ? `${val}万` : `${val.toFixed(1)}万`;
  }
  return num.toString();
}

const onImageError = (e) => {
  e.target.src = 'https://picsum.photos/200/112?random=999'
}
</script>

<style scoped>
.recommend-sidebar {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.video-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.video-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.video-item:hover {
  transform: translateY(-2px);
}

.cover-container {
  position: relative;
  width: 120px;
  height: 67.5px; /* 16:9 */
  border-radius: 6px;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 4px;
}

.info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.author {
  font-size: 12px;
  color: #909399;
  margin: 0 0 4px;
}

.stats {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .video-item {
    flex-direction: column;
    align-items: stretch;
  }

  .cover-container {
    width: 100%;
    height: 56px;
  }

  .info {
    padding-left: 0;
  }
}
</style>
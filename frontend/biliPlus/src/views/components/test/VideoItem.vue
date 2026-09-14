<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const props = defineProps({ item: Object })

// 点击视频跳转详情页
const handleClick = () => {
  router.push(`/video/${props.item.id}`)
}

// 格式化播放量/点赞数
const formatCount = (num) => {
  if (num >= 10000) {
    const val = num / 10000;
    return val % 1 === 0 ? `${val}万` : `${val.toFixed(1)}万`;
  }
  return num.toString();
}
</script>

<template>
  <div class="video-col">
    <div class="video-card" @click="handleClick">
      <!-- 视频封面 -->
      <div class="video-cover">
        <el-image :src="props.item.coverUrl" fit="cover" class="cover-img" />
        <el-tag v-if="props.item.tag" class="video-tag" :type="props.item.tag === '原创' ? 'success' : 'warning'">
          {{ props.item.tag }}
        </el-tag>
        <div class="video-duration">{{ props.item.duration }}</div>
      </div>

      <!-- 视频信息 -->
      <div class="video-info">
        <h4 class="video-title">{{ props.item.title }}</h4>
        <div class="video-author">{{ props.item.author }}</div>
        <div class="video-stats">
          <span>{{ formatCount(props.item.playCount) }}</span>
          <span>• {{ formatCount(props.item.likeCount) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.video-col {
  width: 20%;
  padding: 0 8px 20px;
  box-sizing: border-box;
}

.video-card {
  width: 100%;
  cursor: pointer;
  transition: transform 0.2s;
}
.video-card:hover { transform: translateY(-4px); }

.video-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16/9;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 8px;
}
.cover-img { width: 100%; height: 100%; }

.video-tag {
  position: absolute;
  top: 6px;
  left: 6px;
  font-size: 12px;
  padding: 2px 6px;
}
.video-duration {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(0,0,0,0.7);
  color: #fff;
  font-size: 12px;
  padding: 1px 6px;
  border-radius: 4px;
}

.video-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 6px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 38px;
}
.video-author {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.video-stats {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 4px;
}
</style>
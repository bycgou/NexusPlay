<script setup lang="ts">
// 视频信息+up主
import { ref } from 'vue'

const props = defineProps({
  currentVideo: {
    type: Object,
    required: true
  }
})

// 格式化播放量/粉丝数
const formatCount = (num) => {
  if (num >= 10000) {
    const val = num / 10000;
    return val % 1 === 0 ? `${val}万` : `${val.toFixed(1)}万`;
  }
  return num.toString();
}

</script>

<template>
  <div class="video-info-bar" style="background: #fff; padding: 16px; border-radius: 8px; margin-top: 16px;">
    <h1 class="video-title" style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">{{ currentVideo.title }}</h1>

    <div class="video-meta" style="color: #999; font-size: 14px; margin-bottom: 16px; display: flex; gap: 20px;">
      <span>播放量：{{ formatCount(currentVideo.playCount) }}</span>
      <span>发布时间：{{ currentVideo.publishTime }}</span>
    </div>

    <div class="author-info" style="display: flex; align-items: center; gap: 12px; padding-top: 16px; border-top: 1px solid #eee;">
      <img :src="currentVideo.author.avatar" class="author-avatar" style="width: 50px; height: 50px; border-radius: 50%; object-fit: cover;" />
      <div class="author-detail">
        <div class="author-name" style="font-weight: 500;">{{ currentVideo.author.name }}</div>
        <div class="author-fans" style="font-size: 12px; color: #999;">{{ formatCount(currentVideo.author.fansCount) }} 粉丝</div>
      </div>
      <el-button type="primary">关注</el-button>
    </div>
  </div>
</template>

<style scoped>

</style>
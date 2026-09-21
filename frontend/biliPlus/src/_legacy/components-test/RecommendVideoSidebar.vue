<script setup lang="ts">
// <!--右侧推荐视频列表-->
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const props = defineProps({
  recommendVideos: {
    type: Array,
    required: true
  }
})

// 格式化播放量
const formatCount = (num) => {
  if (num >= 10000) {
    const val = num / 10000;
    return val % 1 === 0 ? `${val}万` : `${val.toFixed(1)}万`;
  }
  return num.toString();
}

// 点击推荐视频跳转
const handleRecommendClick = (item) => {
  router.push(`/video/${item.id}`)
}

</script>

<template>

  <div class="recommend-sidebar" style="width: 100%;">
    <h3 class="recommend-title" style="font-size: 16px; margin-bottom: 16px; padding-left: 4px;">推荐视频</h3>

    <div class="recommend-video-item" v-for="(item, index) in recommendVideos" :key="index" style="margin-bottom: 12px; cursor: pointer;">
      <img :src="item.coverUrl" style="width: 100%; border-radius: 4px; margin-bottom: 8px;" />
      <div class="video-info" style="font-size: 14px;">
        <div class="video-title" style="font-weight: 500; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">{{ item.title }}</div>
        <div class="video-meta" style="color: #999; font-size: 12px; display: flex; justify-content: space-between;">
          <span>{{ item.author }}</span>
          <span>{{ formatCount(item.playCount) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
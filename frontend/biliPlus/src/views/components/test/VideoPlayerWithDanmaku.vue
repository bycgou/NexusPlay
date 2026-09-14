<script setup lang="ts">
// ===========================================视频+弹幕=========================

import { ref } from 'vue'

const props = defineProps({
  currentVideo: {
    type: Object,
    required: true
  }
})

// 假弹幕数据
const danmakus = ref(['这视频太戳我了！', 'UP主分析好透彻！', '学到了！'])
</script>

<template>
  <div class="video-player-wrapper">
    <!-- 视频播放器 -->
    <video
        :src="currentVideo.videoUrl"
        controls
        class="video-player"
        :poster="currentVideo.coverUrl"
        style="width: 100%; aspect-ratio: 16/9; object-fit: cover;"
    >
      您的浏览器不支持视频播放
    </video>

    <!-- 弹幕区域（假数据占位，后续对接真实逻辑） -->
    <div class="danmaku-area" style="padding: 8px 0;">
      <el-tag v-for="(danmaku, index) in danmakus" :key="index" type="info" effect="plain" style="margin-right: 4px;">
        {{ danmaku }}
      </el-tag>
    </div>
  </div>

</template>

<style scoped>
.video-player-wrapper {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}
</style>
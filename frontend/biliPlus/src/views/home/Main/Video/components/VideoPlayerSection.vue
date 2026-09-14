<!-- src/components/video/VideoPlayerSection.vue -->
<template>
  <div class="video-player-section">
    <!-- 视频信息 -->
    <div class="video-info">
      <h1 class="video-title">{{ video.title }}</h1>
      <div class="video-meta">
        <span class="play-count">播放量：{{ formatCount(video.playCount) }}</span>
        <span class="publish-time">发布时间：{{ video.publishTime }}</span>
      </div>
      <div class="author-info">
        <img :src="video.author.avatar" class="author-avatar" />
        <div class="author-detail">
          <div class="author-name">{{ video.author.name }}</div>
          <div class="author-fans">{{ formatCount(video.author.fansCount) }} 粉丝</div>
        </div>
        <button class="follow-btn">关注</button>
      </div>
    </div>
    <!-- 视频播放器 -->
    <div class="video-player">
      <video
          :src="video.videoUrl"
          controls
          class="video"
          :poster="video.coverUrl"
          autoplay
      >
        您的浏览器不支持视频播放
      </video>
    </div>
  </div>
</template>

<script setup>
defineProps({
  video: {
    type: Object,
    required: true
  }
})

const formatCount = (num) => {
  if (num >= 10000) {
    const val = num / 10000;
    return val % 1 === 0 ? `${val}万` : `${val.toFixed(1)}万`;
  }
  return num.toString();
}
</script>

<style scoped>
.video-player {
  background-color: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.video {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: contain;
}

.video-info {
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.video-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.video-meta {
  color: #999;
  font-size: 14px;
  margin-bottom: 16px;
  display: flex;
  gap: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.author-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.author-fans {
  font-size: 12px;
  color: #999;
}

.follow-btn {
  margin-left: auto;
  background-color: #ff4d4f;
  color: #fff;
  border: none;
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
}
.follow-btn:hover {
  background-color: #d93025;
}
</style>
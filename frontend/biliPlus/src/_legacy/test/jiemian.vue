<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// ========== 轮播图数据 ==========
const banners = ref([
  { url: '/banner1.jpg', videoId: 38, title: '龙叔 · 终极任务' },
  { url: '/banner2.jpg', videoId: 37, title: '程翔 · 情感协议' },
  { url: '/banner3.jpg', videoId: 45, title: '悟空 · 数字觉醒' }
])

const goToVideo = (videoId: number) => {
  router.push({ name: 'VideoDetail', params: { id: videoId } })
}

// ========== 分类标签 ==========
const categories = ref([
  '全部', '推荐', '动画', '音乐', '舞蹈', '游戏',
  '知识', '科技', '运动', '汽车', '生活', '美食',
  '动物圈', '鬼畜', '时尚', '娱乐', '影视', '更多'
])
const currentTab = ref(0)
const handleTabClick = (index: number) => {
  currentTab.value = index
}

// ========== 视频卡片数据 ==========
const videos = ref(
    Array.from({ length: 12 }, (_, i) => ({
      id: i + 1,
      title: `这是第 ${i + 1} 个超赞的视频标题，内容非常精彩`,
      cover: `/cover${(i % 5) + 1}.jpg`,
      author: `UP主${i + 1}`,
      views: Math.floor(Math.random() * 1000000),
      duration: `${Math.floor(Math.random() * 10) + 1}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}`
    }))
)
</script>

<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <section class="banner-section">
      <el-carousel
          :interval="5000"
          height="320px"
          indicator-position="none"
          class="banner-carousel"
      >
        <el-carousel-item
            v-for="(item, index) in banners"
            :key="index"
            @click="goToVideo(item.videoId)"
            class="banner-item"
        >
          <div class="banner-image-wrapper">
            <img :src="item.url" :alt="item.title" class="banner-image" />
            <div class="banner-title">{{ item.title }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 分类标签 -->
    <section class="category-section">
      <div class="category-container">
        <div class="tabs-list">
          <el-tag
              v-for="(item, index) in categories"
              :key="index"
              :class="{ 'tab-item': true, 'active': currentTab === index }"
              @click="handleTabClick(index)"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>
    </section>

    <!-- 视频列表 -->
    <section class="video-section">
      <div class="video-container">
        <div class="video-grid">
          <div
              v-for="video in videos"
              :key="video.id"
              class="video-card"
              @click="goToVideo(video.id)"
          >
            <div class="cover-wrapper">
              <img :src="video.cover" :alt="video.title" class="cover" />
              <span class="duration">{{ video.duration }}</span>
            </div>
            <div class="info">
              <h3 class="title">{{ video.title }}</h3>
              <p class="author">{{ video.author }}</p>
              <p class="views">{{ video.views.toLocaleString() }} 播放</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部 -->
    <footer class="site-footer">
      <div class="footer-content">
        © 2025 MyVideoSite. All rights reserved.
      </div>
    </footer>
  </div>
</template>

<style scoped>
.home-page {
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* ========== 轮播图 ========== */
.banner-section {
  padding: 20px 0;
}

.banner-carousel {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  background: #000;
  margin: 0 auto;
  max-width: 1200px;
}

.banner-item {
  cursor: pointer;
  position: relative;
}

.banner-image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: filter 0.3s ease;
}

.banner-item:hover .banner-image {
  filter: brightness(0.95);
}

.banner-title {
  position: absolute;
  bottom: 16px;
  left: 16px;
  color: white;
  font-size: 1.1rem;
  font-weight: 500;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
  z-index: 1;
  max-width: 70%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== 分类标签 ========== */
.category-section {
  background: white;
  padding: 16px 0;
  border-bottom: 1px solid #e5e5e5;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05);
}

.category-container {
  width: 100%;
  display: flex;
  justify-content: center;
}

.tabs-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 0 16px;
  scrollbar-width: thin;
  scrollbar-color: #ddd transparent;
}

.tabs-list::-webkit-scrollbar {
  height: 6px;
}

.tabs-list::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

.tab-item {
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
  border-radius: 20px;
  background-color: #f5f5f5;
  color: #333;
  border: none;
  white-space: nowrap;
  user-select: none;
  transition: all 0.2s ease;
}

.tab-item:hover {
  background-color: #eee;
  transform: translateY(-1px);
}

.tab-item.active {
  background-color: #fb7299;
  color: white;
  font-weight: 500;
}

.tab-item.active:hover {
  background-color: #f85a8a;
}

/* ========== 视频列表 ========== */
.video-section {
  padding: 24px 0;
}

.video-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.video-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.cover-wrapper {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 */
}

.cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.duration {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  font-size: 12px;
  padding: 2px 4px;
  border-radius: 3px;
}

.info {
  padding: 12px;
}

.title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 8px 0;
  line-height: 1.4;
  color: #222;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.author {
  font-size: 12px;
  color: #666;
  margin: 0 0 4px 0;
}

.views {
  font-size: 12px;
  color: #999;
  margin: 0;
}

/* ========== 底部 ========== */
.site-footer {
  background: #1f1f1f;
  color: #aaa;
  text-align: center;
  padding: 20px 0;
  margin-top: 40px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .banner-carousel {
    height: 220px;
  }

  .banner-title {
    font-size: 0.9rem;
    bottom: 12px;
    left: 12px;
  }

  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }

  .title {
    font-size: 13px;
  }
}
</style>
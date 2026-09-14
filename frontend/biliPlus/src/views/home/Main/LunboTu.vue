<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from "vue-router";
import { getBanners } from '@/api/banner'

const router = useRouter()

interface BannerItem {
  id?: number
  url: string
  videoId?: number | null
  title: string
  desc?: string
  linkType?: number
  linkUrl?: string | null
}

// 兜底本地数据（接口失败时仍可展示）
const fallbackImages: BannerItem[] = [
  {
    url: '/longshu-banner.jpg',
    videoId: 38,
    title: '龙叔经典动作集锦',
    desc: '回顾成龙电影中的精彩瞬间'
  },
  {
    url: '/chengxiang-kiss.jpg',
    videoId: 37,
    title: '城乡爱情故事',
    desc: '一段跨越城乡的浪漫情缘'
  },
  {
    url: '/sunwukong.jpg',
    videoId: 45,
    title: '孙悟空传奇',
    desc: '西游记中的经典角色解析'
  }
]

const images = ref<BannerItem[]>([...fallbackImages])
const activeIndex = ref(0)
const isHovered = ref(false)

const loadBanners = async () => {
  try {
    const res = await getBanners()
    if (res.code === 1 && Array.isArray(res.data) && res.data.length > 0) {
      images.value = res.data.map((item: any) => ({
        id: item.id,
        url: item.imageUrl,
        videoId: item.videoId ?? null,
        title: item.title,
        desc: item.description || '',
        linkType: item.linkType ?? 3,
        linkUrl: item.linkUrl || null,
      }))
    }
  } catch (e) {
    console.warn('加载轮播图失败，使用本地兜底数据', e)
  }
}

const goToVideo = (item: BannerItem) => {
  if (item.linkType === 1 && item.videoId) {
    router.push({ name: 'VideoDetail', params: { id: item.videoId } })
    return
  }
  if (item.linkType === 2 && item.linkUrl) {
    window.open(item.linkUrl, '_blank', 'noopener')
    return
  }
  // 旧数据无 linkType 时按 videoId 跳转
  if (item.videoId) {
    router.push({ name: 'VideoDetail', params: { id: item.videoId } })
  }
}

const handleChange = (index: number) => {
  activeIndex.value = index
}

onMounted(loadBanners)
</script>

<template>
  <div
      class="carousel-container"
      @mouseenter="isHovered = true"
      @mouseleave="isHovered = false"
  >
    <el-carousel
        :interval="isHovered ? 0 : 4000"
        type="card"
        :height="'280px'"
        @change="handleChange"
        indicator-position="none"
        :arrow="'never'"
    >
      <el-carousel-item
          v-for="(item, index) in images"
          :key="index"
          class="carousel-item"
          :class="{ 'active-slide': index === activeIndex }"
          @click="goToVideo(item)"
      >
        <div class="carousel-image-wrapper">
          <img
              :src="item.url"
              :alt="item.title"
              class="carousel-image"
              loading="lazy"
          />
          <div class="image-overlay"></div>
          <div class="carousel-caption">
            <div class="caption-tag">精选推荐</div>
            <h3 class="carousel-title">{{ item.title }}</h3>
            <p class="carousel-desc">{{ item.desc }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 自定义指示器 -->
    <div class="carousel-indicators">
      <button
          v-for="(item, index) in images"
          :key="index"
          @click.stop="activeIndex = index"
          :class="{ 'indicator-active': index === activeIndex }"
          :aria-label="`切换到第 ${index + 1} 张`"
      ></button>
    </div>
  </div>
</template>

<style scoped>
.carousel-container {
  position: relative;
  max-width: 1200px;
  margin: var(--space-lg) auto var(--space-xl);
  padding: 0 var(--space-md);
}

.carousel-item {
  cursor: pointer;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.carousel-image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-base);
}

.carousel-item:hover .carousel-image-wrapper {
  box-shadow: var(--shadow-hover);
}

.carousel-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.active-slide .carousel-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
      to top,
      rgba(0, 0, 0, 0.75) 0%,
      rgba(0, 0, 0, 0.2) 50%,
      rgba(0, 0, 0, 0) 100%
  );
  z-index: 1;
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--space-lg);
  color: white;
  z-index: 2;
  transform: translateY(16px);
  opacity: 0;
  transition: all var(--transition-base);
}

.active-slide .carousel-caption {
  transform: translateY(0);
  opacity: 1;
}

.caption-tag {
  display: inline-block;
  background: var(--brand);
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 2px var(--space-sm);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-sm);
  letter-spacing: 1px;
}

.carousel-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 var(--space-xs);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  letter-spacing: -0.3px;
}

.carousel-desc {
  font-size: 14px;
  opacity: 0.85;
  margin: 0;
  max-width: 80%;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

/* 自定义指示器 */
.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: var(--space-sm);
  margin-top: var(--space-md);
}

.carousel-indicators button {
  width: 8px;
  height: 8px;
  border-radius: 4px;
  border: none;
  background-color: var(--line);
  cursor: pointer;
  transition: all var(--transition-fast);
  padding: 0;
}

.carousel-indicators button:hover {
  background-color: var(--mist-light);
}

.carousel-indicators button.indicator-active {
  width: 24px;
  background-color: var(--brand);
}

/* 响应式 */
@media (max-width: 768px) {
  .carousel-container {
    margin: var(--space-md) auto var(--space-lg);
    padding: 0 var(--space-sm);
  }

  .carousel-title {
    font-size: 16px;
  }

  .carousel-desc {
    font-size: 12px;
  }

  .carousel-caption {
    padding: var(--space-md);
  }
}
</style>
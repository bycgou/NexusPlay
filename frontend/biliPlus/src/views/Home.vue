<script setup lang="ts">
import LunboTu from "@/views/home/Main/LunboTu.vue";
import Categry from "@/views/home/Main/Categry.vue";
import Video from "@/views/home/Main/Video/Video.vue";
import HotRankPanel from "@/views/home/Main/HotRankPanel.vue";
import Footer from "@/views/home/Footer.vue";
import { ref } from 'vue'

const categoryId = ref<number | null>(null)

const handleCategoryChange = (id: number | null) => {
  categoryId.value = id
}
</script>

<template>
  <div class="home-page">
    <!-- 轮播图 + 热搜榜 两列 -->
    <section class="home-hero">
      <div class="hero-carousel">
        <LunboTu />
      </div>
      <aside class="hero-side">
        <HotRankPanel />
      </aside>
    </section>

    <Categry @change="handleCategoryChange"></Categry>

    <!-- 全宽视频推荐，5 列放大 -->
    <Video :category-id="categoryId"></Video>

    <Footer></Footer>
  </div>
</template>

<style scoped>
.home-page {
  background: var(--paper);
  min-height: calc(100vh - 60px);
}

.home-hero {
  max-width: 1440px;
  margin: 0 auto;
  padding: var(--space-md, 16px) var(--space-md, 16px) 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: var(--space-md, 16px);
  align-items: stretch;
}

.hero-carousel {
  min-width: 0;
}

/* 轮播图容器去掉自带外边距/内边距，吃满左栏 */
.hero-carousel :deep(.carousel-container) {
  margin: 0;
  padding: 0;
  max-width: none;
  height: 300px;
}

.hero-side {
  min-width: 0;
  height: 300px;
}

.hero-side :deep(.hot-rank-panel) {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px 14px 8px;
  box-sizing: border-box;
}

.hero-side :deep(.rank-list) {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

@media (max-width: 1100px) {
  .home-hero {
    grid-template-columns: minmax(0, 1fr) 280px;
  }

  .hero-side {
    height: 280px;
  }

  .hero-carousel :deep(.carousel-container) {
    height: 280px;
  }
}

@media (max-width: 900px) {
  .home-hero {
    grid-template-columns: 1fr;
  }

  .hero-side {
    height: auto;
    max-height: 360px;
  }

  .hero-side :deep(.hot-rank-panel) {
    max-height: 360px;
  }
}
</style>

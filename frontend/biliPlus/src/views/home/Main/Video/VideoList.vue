<template>
  <div class="video-list-container">
    <!-- 标题区域 -->
    <div class="list-header">
      <h2 class="list-title">
        <span class="title-accent"></span>
        <slot name="title">推荐视频</slot>
      </h2>
      <button
          v-if="!loading && videoData.length > 0"
          class="view-more"
          @click="$emit('view-more')"
      >
        查看更多
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>

    <!-- 加载中：显示骨架屏 -->
    <VideoSkeleton v-if="loading" :count="10" />

    <!-- 空状态 -->
    <EmptyState
        v-else-if="videoData.length === 0"
        icon="VideoCamera"
        title="暂无视频"
        description="快去投稿你的第一个视频吧"
    />

    <!-- 视频网格 -->
    <div v-else class="video-grid">
      <template v-for="item in videoData" :key="item.id">
        <slot name="video-item" :item="item"></slot>
      </template>
    </div>
  </div>
</template>

<script setup>
import VideoSkeleton from '@/components/VideoSkeleton.vue'
import EmptyState from '@/components/EmptyState.vue'

const props = defineProps({
  videoData: {
    type: Array,
    required: true,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
});
</script>

<style scoped>
.video-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-lg) var(--space-md);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}

.list-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--ink);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.title-accent {
  width: 4px;
  height: 20px;
  background: var(--brand);
  border-radius: 2px;
}

.view-more {
  color: var(--brand);
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.view-more:hover {
  background: var(--brand-bg);
  color: var(--brand-dark);
}

.view-more .el-icon {
  transition: transform var(--transition-fast);
}

.view-more:hover .el-icon {
  transform: translateX(2px);
}

.video-grid {
  display: flex;
  flex-wrap: wrap;
  margin: 0 calc(-1 * var(--space-sm));
}
</style>
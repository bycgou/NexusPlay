<script setup lang="ts">
defineProps({
  count: {
    type: Number,
    default: 10
  }
})
</script>

<template>
  <div class="skeleton-grid">
    <div v-for="i in count" :key="i" class="skeleton-card">
      <!-- 封面骨架 -->
      <div class="skeleton-cover">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" class="cover-skeleton" />
          </template>
        </el-skeleton>
      </div>

      <!-- 信息骨架 -->
      <div class="skeleton-info">
        <el-skeleton :rows="3" animated>
          <template #template>
            <el-skeleton-item variant="h3" style="width: 90%; height: 16px; margin-bottom: 8px;" />
            <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
              <el-skeleton-item variant="text" style="width: 40%; height: 12px;" />
              <el-skeleton-item variant="text" style="width: 20%; height: 12px;" />
            </div>
            <div style="display: flex; gap: 12px;">
              <el-skeleton-item variant="text" style="width: 25%; height: 12px;" />
              <el-skeleton-item variant="text" style="width: 25%; height: 12px;" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-grid {
  display: flex;
  flex-wrap: wrap;
  margin: 0 calc(-1 * var(--space-sm));
}

.skeleton-card {
  width: 20%;
  padding: 0 var(--space-sm) var(--space-lg);
  box-sizing: border-box;
}

.skeleton-cover {
  width: 100%;
  aspect-ratio: 16/9;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: var(--space-md);
}

.cover-skeleton {
  width: 100%;
  height: 100%;
}

.skeleton-info {
  padding: 0 var(--space-xs);
}

/* 响应式 */
@media (max-width: 1200px) {
  .skeleton-card {
    width: 25%;
  }
}

@media (max-width: 768px) {
  .skeleton-card {
    width: 50%;
  }
}

@media (max-width: 480px) {
  .skeleton-card {
    width: 100%;
  }
}
</style>

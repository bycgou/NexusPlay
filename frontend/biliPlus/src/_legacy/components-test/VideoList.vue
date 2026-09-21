<script setup lang="ts">
const props = defineProps({
  videoData: {
    type: Array,
    required: true,
    default: () => []
  }
});
</script>

<template>
  <div class="video-list-container">
    <!-- 标题区域 -->
    <div class="list-header">
      <h2 class="list-title">
        <slot name="title">推荐视频</slot>
      </h2>
      <el-button type="text" @click="$emit('view-more')">
        查看更多 <i class="el-icon-arrow-right"></i>
      </el-button>
    </div>

    <!-- 视频网格 -->
    <div class="video-grid">
      <slot
          name="video-item"
          v-for="item in videoData"
          :key="item.id"
          :item="item"
      ></slot>
    </div>
  </div>
</template>

<style scoped>
.video-list-container {
  padding: 0 20px;
  width: 100%;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.list-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.video-grid {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -8px;
}
</style>
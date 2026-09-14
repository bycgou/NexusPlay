<script setup lang="ts">
defineProps({
  icon: {
    type: String,
    default: 'Box'
  },
  title: {
    type: String,
    default: '暂无数据'
  },
  description: {
    type: String,
    default: ''
  },
  actionText: {
    type: String,
    default: ''
  }
})

defineEmits(['action'])
</script>

<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="48"><component :is="icon" /></el-icon>
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <el-button
        v-if="actionText"
        type="primary"
        class="empty-action"
        @click="$emit('action')"
    >
      {{ actionText }}
    </el-button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl) var(--space-lg);
  text-align: center;
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: var(--brand-bg);
  color: var(--brand-light);
  margin-bottom: var(--space-lg);
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 var(--space-sm);
}

.empty-desc {
  font-size: 14px;
  color: var(--mist);
  margin: 0 0 var(--space-lg);
  max-width: 280px;
}

.empty-action {
  margin-top: var(--space-sm);
}
</style>

<template>
  <!-- 分类标签容器 -->
  <div class="category-tabs">
    <el-row class="tabs-row">
      <el-col :span="24">
        <!-- 标签列表 -->
        <div class="tabs-list">
          <el-tag
              class="tab-item"
              :class="{ active: currentId === null }"
              @click="handleTabClick(null)"
          >
            全部
          </el-tag>
          <el-tag
              v-for="item in categories"
              :key="item.id"
              class="tab-item"
              :class="{ active: currentId === item.id }"
              @click="handleTabClick(item.id)"
          >
            {{ item.name }}
          </el-tag>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategories } from '@/api/category'

const emit = defineEmits(['change'])

const categories = ref([])
const currentId = ref(null)

const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.code === 1 && Array.isArray(res.data)) {
      categories.value = res.data
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const handleTabClick = (id) => {
  currentId.value = id
  emit('change', id)
}

onMounted(loadCategories)
</script>

<style scoped>
.category-tabs {
  background-color: var(--paper-white);
  padding: var(--space-sm) 0;
  border-bottom: 1px solid var(--line);
}

.tabs-row {
  max-width: 1200px;
  margin: 0 auto;
}

.tabs-list {
  display: flex;
  align-items: center;
  width: 100%;
  overflow-x: auto;
  padding: 0 var(--space-md);
  gap: var(--space-sm);
  scrollbar-width: none;
}

.tabs-list::-webkit-scrollbar {
  display: none;
}

.tab-item {
  padding: var(--space-sm) var(--space-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
  background-color: var(--paper);
  color: var(--ink-secondary);
  border: none;
  white-space: nowrap;
}

.tab-item.active {
  background-color: var(--brand);
  color: #fff;
}
</style>

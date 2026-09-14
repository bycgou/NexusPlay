<!-- src/views/contribution/components/TagEditor.vue -->
<template>
  <div>
    <div class="tag-container">
      <el-tag
          v-for="(tag, index) in modelValue"
          :key="index"
          closable
          @close="removeTag(index)"
          class="tag-item"
      >
        {{ tag }}
      </el-tag>

      <el-input
          v-if="showTagInput"
          v-model="newTag"
          ref="tagInput"
          size="small"
          @keyup.enter="addTag"
          @blur="addTag"
          class="tag-input"
          placeholder="输入标签"
      />

      <el-button
          v-else
          size="small"
          type="text"
          @click="showTagInput = true"
          :disabled="modelValue.length >= 10"
          class="add-tag-btn"
      >
        <el-icon><Plus /></el-icon> 添加标签
      </el-button>
    </div>

    <div class="recommend-tags">
      <span class="recommend-label">推荐标签：</span>
      <el-button
          v-for="tag in recommendTags"
          :key="tag"
          type="text"
          size="small"
          @click="addTag(tag)"
          :disabled="modelValue.includes(tag) || modelValue.length >= 10"
          class="recommend-tag"
      >
        {{ tag }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, nextTick, watch } from 'vue';
import { Plus } from '@element-plus/icons-vue';

const props = defineProps({
  modelValue: Array,
  recommendTags: Array
});

const emit = defineEmits(['update:modelValue']);

const showTagInput = ref(false);
const newTag = ref('');
const tagInput = ref(null);

const addTag = (tag) => {
  const val = tag ? tag.trim() : newTag.value.trim();
  if (val && !props.modelValue.includes(val) && props.modelValue.length < 10) {
    emit('update:modelValue', [...props.modelValue, val]);
  }
  newTag.value = '';
  showTagInput.value = false;
};

const removeTag = (index) => {
  const updated = [...props.modelValue];
  updated.splice(index, 1);
  emit('update:modelValue', updated);
};

watch(showTagInput, (val) => {
  if (val) nextTick(() => tagInput.value?.focus());
});
</script>

<style scoped>
.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}
.tag-item { margin-right: 8px; }
.tag-input { width: 120px; }
.add-tag-btn { color: #409eff; }
.recommend-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.recommend-label { font-size: 14px; color: #909399; }
.recommend-tag { color: #409eff; padding: 0 4px; }
</style>
<!-- src/views/contribution/components/ContributionForm.vue -->
<template>
  <el-form
      ref="formRef"
      :model="localForm"
      :rules="rules"
      class="contribution-form"
  >
    <el-form-item label="视频封面" class="form-item">
      <slot name="cover-selector"></slot>
    </el-form-item>

    <el-form-item label="视频标题" prop="title" class="form-item">
      <el-input
          v-model="localForm.title"
          placeholder="请输入吸引人的标题（最多80字）"
          maxlength="80"
          show-word-limit
          class="title-input"
      />
    </el-form-item>

    <el-form-item label="内容分类" prop="category" class="form-item">
      <el-select
          v-model="localForm.category"
          placeholder="请选择分类"
          class="category-select"
          :loading="categoriesLoading"
      >
        <el-option
            v-for="item in categories"
            :key="item.id"
            :label="item.name"
            :value="item.id"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="创作类型" prop="type" class="form-item">
      <el-radio-group v-model="localForm.type" class="type-radio">
        <el-radio label="original">原创</el-radio>
        <el-radio label="reprint">转载</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="视频标签" prop="tags" class="form-item">
      <TagEditor
          v-model="localForm.tags"
          :recommend-tags="recommendTags"
      />
    </el-form-item>

    <el-form-item label="视频简介" prop="description" class="form-item">
      <el-input
          v-model="localForm.description"
          type="textarea"
          :rows="5"
          placeholder="分享视频的详细信息，帮助更多人发现你的作品"
          maxlength="500"
          show-word-limit
          class="description-input"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, computed } from 'vue';
import TagEditor from './TagEditor.vue';

const props = defineProps({
  modelValue: Object,
  rules: Object,
  recommendTags: Array,
  isUploading: Boolean,
  /** 后台视频分区列表 */
  categories: {
    type: Array,
    default: () => []
  },
  categoriesLoading: Boolean
});

const emit = defineEmits(['update:modelValue']);

// 使用 computed 实现双向绑定
const localForm = computed({
  get() {
    return props.modelValue;
  },
  set(value) {
    emit('update:modelValue', value);
  }
});

// 暴露 validate 方法给父组件
const formRef = ref();
defineExpose({
  validate: () => formRef.value?.validate()
});
</script>

<style scoped>
.contribution-form { padding: 20px; }
.form-item { margin-bottom: 24px; }
.title-input,
.category-select,
.description-input { width: 100%; }
.type-radio { display: flex; gap: 20px; }
</style>

<!-- src/views/contribution/components/CoverSelector.vue -->
<template>
  <div class="cover-selector">
    <div
        class="cover-item"
        v-for="(cover, index) in localCoverOptions"
        :key="index"
        @click="selectCover(index)"
        :class="{ active: cover.url === modelValue }"
    >
      <img :src="cover.url" :alt="'封面' + (index + 1)" class="cover-img">
      <div v-if="cover.url === modelValue" class="cover-check">
        <el-icon><Check /></el-icon>
      </div>
    </div>

    <div class="cover-upload" @click="coverInput.click()">
      <input
          type="file"
          ref="coverInput"
          accept="image/*"
          @change="handleCoverUpload"
          class="cover-input"
      >
      <el-icon class="upload-icon"><Upload /></el-icon>
      <p>上传封面</p>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue';
import { Check, Upload } from '@element-plus/icons-vue';
import axios from "axios";
const props = defineProps({
  modelValue: String
});

const emit = defineEmits(['update:modelValue']);

const coverInput = ref(null);
const defaultCovers = [
  { url: 'https://picsum.photos/300/170?random=1' },
  { url: 'https://picsum.photos/300/170?random=2' },
  { url: 'https://picsum.photos/300/170?random=3' }
];
const localCoverOptions = ref([...defaultCovers]);

onMounted(() => {
  if (!props.modelValue && defaultCovers.length > 0) {
    emit('update:modelValue', defaultCovers[0].url);
  }
});

const selectCover = (index) => {
  emit('update:modelValue', localCoverOptions.value[index].url);
};

const handleCoverUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  // 校验文件类型和大小（可选）
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件');
    return;
  }
  if (file.size > 5 * 1024 * 1024) { // 5MB
    ElMessage.error('封面不能超过 5MB');
    return;
  }

  const formData = new FormData();
  formData.append('file', file);

  try {
    // 调用你的封面上传接口（需后端实现）
    const token = localStorage.getItem('token');
    const res = await axios.post('/api/pp/upload/cover', formData, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    });
    console.log('响应的图片地址:',res.data)
    if (res.data?.code === 200|| res.data?.code === 1) {
      const url = res.data.data;
      const newCover = { url };
      localCoverOptions.value.unshift(newCover);
      emit('update:modelValue', url); // ← 存的是 https://xxx.com/cover/abc.jpg
    } else {
      throw new Error('返回图片地址为空');
    }
  } catch (err) {
    ElMessage.error('封面上传失败，请重试');
    console.error(err);
  } finally {
    e.target.value = ''; // 清空 input，允许重复选择同一文件
  }
};
</script>

<style scoped>
.cover-selector {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.cover-item {
  position: relative;
  width: 150px;
  height: 85px;
  border: 2px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
}
.cover-item.active { border-color: #409eff; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.cover-check {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #409eff;
  color: white;
  padding: 4px;
  border-bottom-left-radius: 4px;
}
.cover-upload {
  width: 150px;
  height: 85px;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}
.cover-upload:hover { border-color: #409eff; }
.cover-input { display: none; }
.upload-icon { font-size: 24px; color: #909399; margin-bottom: 4px; }
</style>
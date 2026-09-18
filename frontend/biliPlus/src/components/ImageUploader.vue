<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = withDefaults(defineProps<{
  modelValue?: string
  tip?: string
  maxSizeMb?: number
}>(), {
  modelValue: '',
  tip: '点击上传封面',
  maxSizeMb: 5
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const inputRef = ref<HTMLInputElement | null>(null)
const loading = ref(false)
const url = computed({
  get: () => props.modelValue || '',
  set: (v: string) => emit('update:modelValue', v)
})

const pickFile = () => {
  if (!loading.value) inputRef.value?.click()
}

const handleChange = async (e: Event) => {
  const el = e.target as HTMLInputElement
  const file = el.files?.[0]
  el.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size > props.maxSizeMb * 1024 * 1024) {
    ElMessage.error(`图片不能超过 ${props.maxSizeMb}MB`)
    return
  }
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    // 必须走 /api 代理 + multipart（request 默认 Content-Type 是 json，会破坏 boundary）
    const res: any = await request.post('/pp/upload/cover', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res?.code === 1 || res?.code === 200) {
      url.value = res.data
      ElMessage.success('上传成功')
    } else {
      throw new Error(res?.msg || '上传失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.message || '上传失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="cover-upload">
    <div class="preview" @click="pickFile">
      <img v-if="url" :src="url" class="img" alt="" />
      <div v-else class="placeholder">
        <el-icon size="22"><Upload /></el-icon>
        <span>{{ loading ? '上传中...' : tip }}</span>
      </div>
      <div v-if="url && !loading" class="overlay">更换封面</div>
    </div>
    <input ref="inputRef" type="file" accept="image/*" class="hidden-input" @change="handleChange" />
  </div>
</template>

<style scoped>
.cover-upload { width: 100%; max-width: 280px; }
.preview {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview:hover { border-color: #fb7299; }
.img { width: 100%; height: 100%; object-fit: cover; }
.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 12px;
}
.overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.15s;
}
.preview:hover .overlay { opacity: 1; }
.hidden-input { display: none; }
</style>

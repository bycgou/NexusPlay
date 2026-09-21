<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'

const props = withDefaults(defineProps<{
  modelValue?: string
  tip?: string
  accept?: string
  maxSizeMb?: number
  disabled?: boolean
}>(), {
  modelValue: '',
  tip: '点击上传图片',
  accept: 'image/*',
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
  if (props.disabled || loading.value) return
  inputRef.value?.click()
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
    const uploaded = await uploadImage(file)
    if (!uploaded) throw new Error('上传失败')
    url.value = uploaded
    ElMessage.success('上传成功')
  } catch (err: any) {
    ElMessage.error(err?.message || '上传失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="image-upload">
    <div class="preview" @click="pickFile">
      <img v-if="url" :src="url" class="img" alt="" />
      <div v-else class="placeholder" v-loading="loading">
        <el-icon size="22"><Upload /></el-icon>
        <span class="tip">{{ loading ? '上传中...' : tip }}</span>
      </div>
      <div v-if="url && !loading" class="overlay">更换</div>
    </div>
    <div v-if="url" class="url-row">
      <el-input v-model="url" size="small" placeholder="图片地址" />
      <el-button size="small" text @click="url = ''">清除</el-button>
    </div>
    <input
        ref="inputRef"
        type="file"
        :accept="accept"
        class="hidden-input"
        @change="handleChange"
    />
  </div>
</template>

<style scoped>
.image-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  max-width: 280px;
}
.preview {
  position: relative;
  width: 100%;
  max-width: 220px;
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
.preview:hover {
  border-color: #fb7299;
}
.img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
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
.preview:hover .overlay {
  opacity: 1;
}
.url-row {
  display: flex;
  gap: 4px;
  align-items: center;
}
.hidden-input {
  display: none;
}
</style>

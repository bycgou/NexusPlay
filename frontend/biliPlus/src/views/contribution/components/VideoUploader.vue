<template>
  <div class="upload-container">
    <div
        class="upload-area"
        @click="showFileDialog"
        :class="{ 'uploaded': videoFile, 'uploading': isUploading }"
        :style="{ pointerEvents: isUploading ? 'none' : 'auto', opacity: isUploading ? 0.7 : 1 }"
    >
      <input
          type="file"
          ref="fileInput"
          class="file-input"
          accept="video/*"
          @change="handleFileSelect"
          :disabled="isUploading"
      />

      <div v-if="!videoFile" class="upload-placeholder">
        <el-icon class="upload-icon"><VideoCamera /></el-icon>
        <p class="upload-text">点击或拖拽视频文件至此处上传</p>
        <p class="upload-hint">支持 MP4、AVI、MOV 格式，最大 2GB</p>
      </div>

      <div v-else class="video-preview">
        <!-- 视频 + 上传遮罩包裹容器 -->
        <div class="preview-wrapper">
          <video
              :src="videoUrl"
              controls
              class="preview-video"
          ></video>
          <div v-if="isUploading" class="uploading-overlay">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <p>上传中... {{ uploadProgress }}%</p>
          </div>
        </div>

        <div class="video-info">
          <p class="file-name">{{ videoFile.name }}</p>
          <p class="file-size">{{ formatFileSize(videoFile.size) }}</p>
          <div class="video-actions">
            <el-button
                type="text"
                class="replace-btn"
                @click="showFileDialog"
                :disabled="isUploading"
            >
              更换视频
            </el-button>
            <div v-if="modelValue" class="upload-success">
              <el-icon color="#67C23A"><SuccessFilled /></el-icon>
              <span>上传成功</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-progress
        v-if="uploadProgress > 0 && uploadProgress < 100"
        :percentage="uploadProgress"
        :stroke-width="3"
        class="upload-progress"
    />
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { ElMessage } from 'element-plus';
import SparkMD5 from 'spark-md5';
import axios from 'axios';
import { VideoCamera, Loading, SuccessFilled } from '@element-plus/icons-vue';

const props = defineProps({
  modelValue: String, // uploadedFileUrl
  duration: Number
});

const emit = defineEmits(['update:modelValue', 'update:duration', 'upload-start', 'upload-complete']);

const fileInput = ref(null);
const videoFile = ref(null);
const videoUrl = ref('');
const isUploading = ref(false);
const uploadProgress = ref(0);
const chunkSize = 5 * 1024 * 1024;

const showFileDialog = () => {
  if (!isUploading.value) fileInput.value.click();
};

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const calculateMD5 = (file) => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    const spark = new SparkMD5.ArrayBuffer();
    let offset = 0;
    const loadNext = () => {
      const end = Math.min(offset + chunkSize, file.size);
      reader.readAsArrayBuffer(file.slice(offset, end));
    };
    reader.onload = (e) => {
      spark.append(e.target.result);
      offset += chunkSize;
      if (offset < file.size) loadNext();
      else resolve(spark.end());
    };
    reader.onerror = () => resolve('');
    loadNext();
  });
};

const startUpload = async (file, md5) => {
  const token = localStorage.getItem('token');
  const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};
  const total = Math.ceil(file.size / chunkSize);
  let uploaded = 0;
  for (let i = 0; i < total; i++) {
    const start = i * chunkSize;
    const end = Math.min(start + chunkSize, file.size);
    const chunk = file.slice(start, end);
    const formData = new FormData();
    formData.append('fileInfo', JSON.stringify({
      md5,
      chunkIndex: i,
      totalChunks: total,
      fileName: file.name,
      fileType: file.type
    }));
    formData.append('chunkData', chunk);
    await axios.post('/api/pp/upload/chunk', formData, {
      headers: { 'Content-Type': 'multipart/form-data', ...authHeaders },
      onUploadProgress: (e) => {
        const p = (e.loaded / e.total) * (100 / total);
        uploadProgress.value = Math.floor((uploaded * 100) / total + p);
      }
    });
    uploaded++;
  }

  const mergeRes = await axios.post('/api/pp/upload/merge', {
    md5,
    fileName: file.name,
    totalChunks: total
  }, { headers: authHeaders });

  if (mergeRes.data?.data) {
    emit('update:modelValue', mergeRes.data.data.fileUrl);
    emit('update:duration', mergeRes.data.data.duration);
    emit('upload-complete', { url: mergeRes.data.data.fileUrl, duration: mergeRes.data.data.duration });
  } else throw new Error('合并失败');
};

const handleFileSelect = async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  if (file.size > 2 * 1024 * 1024 * 1024) return ElMessage.error('视频大小不能超过 2GB');
  const validTypes = ['video/mp4', 'video/avi', 'video/mov'];
  if (!validTypes.includes(file.type)) return ElMessage.error('仅支持 MP4、AVI、MOV 格式的视频');

  videoFile.value = file;
  videoUrl.value = URL.createObjectURL(file);
  emit('upload-start');
  isUploading.value = true;
  uploadProgress.value = 0;

  try {
    const md5 = await calculateMD5(file);
    if (!md5) throw new Error('MD5计算失败');
    await startUpload(file, md5);
    ElMessage.success('视频上传完成');
  } catch (err) {
    console.error('上传失败:', err);
    ElMessage.error('上传失败: ' + (err.message || '未知错误'));
  } finally {
    // ✅ 确保无论成功失败都关闭 loading 状态
    isUploading.value = false;
  }
};
</script>

<style scoped>
.upload-container {
  padding: 20px;
}
.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
  position: relative; /* 可选，增强稳定性 */
}
.upload-area:hover { border-color: #409eff; }
.upload-area.uploaded { border-style: solid; border-color: #67c23a; }
.file-input { display: none; }
.upload-placeholder .upload-icon { font-size: 48px; color: #c0c4cc; margin-bottom: 16px; }
.upload-text { font-size: 16px; color: #606266; margin-bottom: 8px; }
.upload-hint { font-size: 14px; color: #909399; }

.video-preview {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 👇 新增：包裹视频和遮罩 */
.preview-wrapper {
  position: relative;
  width: 300px;
  height: 170px;
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
  background-color: #000;
}

.uploading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
}

.video-info {
  flex: 1;
  text-align: left;
}
.file-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}
.file-size {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}
.upload-progress {
  margin-top: 20px;
}
</style>
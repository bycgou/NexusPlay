<template>
  <div class="contribution-page">
    <!-- 顶部导航 -->
    <el-header class="page-header">
      <div class="header-content">
        <el-button type="text" @click="handleBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h1 class="page-title">视频投稿</h1>
      </div>
    </el-header>

    <main class="main-content">
      <!-- 视频上传区域 -->
      <el-card class="upload-section">
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
            >

            <!-- 未上传状态 -->
            <div v-if="!videoFile" class="upload-placeholder">
              <el-icon class="upload-icon"><VideoCamera /></el-icon>
              <p class="upload-text">点击或拖拽视频文件至此处上传</p>
              <p class="upload-hint">支持 MP4、AVI、MOV 格式，最大 2GB</p>
            </div>

            <!-- 上传/预览状态 -->
            <div v-else class="video-preview">
              <div v-if="isUploading" class="uploading-overlay">
                <el-icon class="loading-icon"><Loading /></el-icon>
                <p>上传中... {{ uploadProgress }}%</p>
              </div>
              <video
                  :src="videoUrl"
                  controls
                  class="preview-video"
                  :class="{ 'uploading': isUploading }"
                  :poster="selectedCoverUrl"
              ></video>
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
                  <div v-if="uploadedFileUrl" class="upload-success">
                    <el-icon color="#67C23A"><SuccessFilled /></el-icon>
                    <span>上传成功</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 上传进度条 -->
          <el-progress
              v-if="uploadProgress > 0 && uploadProgress < 100"
              :percentage="uploadProgress"
              stroke-width="3"
              class="upload-progress"
              :show-text="true"
          />
        </div>
      </el-card>

      <!-- 内容编辑区域 -->
      <el-card class="edit-section">
        <el-form
            ref="contributionForm"
            :model="form"
        :rules="formRules"
        class="contribution-form"
        >
        <!-- 封面选择 -->
        <el-form-item label="视频封面" class="form-item">
          <div class="cover-selector">
            <div
                class="cover-item"
                v-for="(cover, index) in coverOptions"
                :key="index"
                @click="selectCover(index)"
                :class="{ active: selectedCoverIndex === index }"
            >
              <img :src="cover.url" :alt="'封面' + (index + 1)" class="cover-img">
              <div v-if="selectedCoverIndex === index" class="cover-check">
                <el-icon><Check /></el-icon>
              </div>
            </div>

            <div class="cover-upload">
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
        </el-form-item>

        <!-- 视频标题 -->
        <el-form-item
            label="视频标题"
            prop="title"
            class="form-item"
        >
          <el-input
              v-model="form.title"
          placeholder="请输入吸引人的标题（最多80字）"
          maxlength="80"
          show-word-limit
          class="title-input"
          />
        </el-form-item>

        <!-- 内容分类 -->
        <el-form-item
            label="内容分类"
            prop="category"
            class="form-item"
        >
          <el-select
              v-model="form.category"
          placeholder="请选择分类"
          class="category-select"
          >
          <el-option label="音乐" value="music" />
          <el-option label="舞蹈" value="dance" />
          <el-option label="游戏" value="game" />
          <el-option label="科技" value="tech" />
          <el-option label="生活" value="life" />
          <el-option label="美食" value="food" />
          <el-option label="旅行" value="travel" />
          </el-select>
        </el-form-item>

        <!-- 创作类型 -->
        <el-form-item
            label="创作类型"
            prop="type"
            class="form-item"
        >
          <el-radio-group v-model="form.type" class="type-radio">
            <el-radio label="original">原创</el-radio>
            <el-radio label="reprint">转载</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 标签管理 -->
        <el-form-item
            label="视频标签"
            prop="tags"
            class="form-item"
        >
          <div class="tag-container">
            <el-tag
                v-for="(tag, index) in form.tags"
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
                :disabled="form.tags.length >= 10"
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
                :disabled="form.tags.includes(tag) || form.tags.length >= 10"
            class="recommend-tag"
            >
            {{ tag }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 视频简介 -->
        <el-form-item
            label="视频简介"
            prop="description"
            class="form-item"
        >
          <el-input
              v-model="form.description"
          type="textarea"
          rows="5"
          placeholder="分享视频的详细信息，帮助更多人发现你的作品"
          maxlength="500"
          show-word-limit
          class="description-input"
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button
              type="primary"
              @click="handleSubmit"
              class="submit-btn"
          >
            发布视频
          </el-button>
          <el-button
              type="default"
              @click="handleSaveDraft"
              class="draft-btn"
          >
            保存草稿
          </el-button>
        </div>
        </el-form>
      </el-card>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue';
import {
  ArrowLeft, VideoCamera, Upload, Check, Plus, Loading, SuccessFilled
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import router from "@/router/index.ts";
import SparkMD5 from 'spark-md5'
import axios from "axios";

// 响应式数据
const videoFile = ref(null);  // 选中的视频文件
const uploadProgress = ref(0); // 上传进度
const chunkSize = 5 * 1024 * 1024; // 分片大小:5MB
const fileMd5 = ref(''); // 文件唯一标识(MD5)
const videoUrl = ref('');
const showTagInput = ref(false);
const newTag = ref('');
const tagInput = ref(null);
const fileInput = ref(null);
const coverInput = ref(null);
const contributionForm = ref(null);
const isUploading = ref(false); // 上传状态标志
const uploadedFileUrl = ref(''); // 上传完成后返回的视频URL
const duration = ref('') // 上传完成后返回的视频时长


//=================================================== 视频上传相关=========================================================
// --------------------------------------------------------------------------------------------------------------

// 选择视频文件
const handleFileSelect = async (e) => {
  console.log("1.选择文件触发",e.target.files)
  const file = e.target.files[0];
  if (file) {
    console.log('【2】选中文件:', file.name, file.type, file.size);
    // 验证文件大小
    if (file.size > 2 * 1024 * 1024 * 1024) { // 统一为2GB
      ElMessage.error('视频大小不能超过 2GB');
      return;
    }

    // 验证文件类型
    const validTypes = ['video/mp4', 'video/avi', 'video/mov'];
    if (!validTypes.includes(file.type)) {
      console.log('【3】文件类型不支持:', file.type);
      ElMessage.error('仅支持 MP4、AVI、MOV 格式的视频');
      return;
    }

    videoFile.value = file;
    videoUrl.value = URL.createObjectURL(file);

    // 开始上传流程
    await startUploadProcess(file);
  }
};

const showFileDialog = () => {
  if (!isUploading.value) {
    fileInput.value.click();
  }
};

// 计算MD5(大文件分片上传,避免卡顿)
const calculateMD5 = (file) => {
  return new Promise((resolve) => {
    const fileReader = new FileReader();
    const spark = new SparkMD5.ArrayBuffer();
    let offset = 0;  // 读取文件的偏移量

    // 分片读取文件并计算MD5
    const loadNextChunk = () => {
      const end = Math.min(offset + chunkSize, file.size);
      fileReader.readAsArrayBuffer(file.slice(offset, end));
    };

    fileReader.onload = (e) => {
      spark.append(e.target.result);
      offset += chunkSize;

      if (offset < file.size) {
        loadNextChunk(); // 继续读取下一分片
      } else {
        const md5 = spark.end(); // 最终MD5
        console.log("文件MD5", md5);
        resolve(md5);
      }
    };

    fileReader.onerror = () => {
      console.error('MD5计算失败');
      resolve('');
    };

    loadNextChunk();
  });
};

// 上传流程
const startUploadProcess = async (file) => {
  if (isUploading.value) {
    ElMessage.warning('已有文件正在上传，请等待完成');
    return;
  }

  try {
    isUploading.value = true;
    uploadProgress.value = 0;

    // 计算文件MD5
    ElMessage.info('正在计算文件MD5...');
    const md5 = await calculateMD5(file);
    fileMd5.value = md5;

    if (!md5) {
      throw new Error('MD5计算失败');
    }

    // 开始分片上传
    await startUpload(file, md5);

  } catch (error) {
    console.error('上传流程出错:', error);
    ElMessage.error('上传失败: ' + error.message);
    isUploading.value = false;
  }
};

// 开始分片上传
const startUpload = async (file, md5) => {
  const totalChunks = Math.ceil(file.size / chunkSize); // 总片数
  let uploadedChunks = 0; // 已上传片数

  ElMessage.info(`开始上传，共 ${totalChunks} 个分片`);

  try {
    // 遍历所有分片，逐个上传
    for (let chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
      const start = chunkIndex * chunkSize;
      const end = Math.min(start + chunkSize, file.size);
      const chunk = file.slice(start, end); // 当前分片二进制数据

      // 构建分片上传的FormData - 与后端接口匹配
      const formData = new FormData();

      // 创建文件信息对象 - 与后端ChunkUploadDTO匹配
      const fileInfo = {
        md5: md5,
        chunkIndex: chunkIndex,
        totalChunks: totalChunks,
        fileName: file.name,
        fileType: file.type
      };

      formData.append('fileInfo', JSON.stringify(fileInfo));
      formData.append('chunkData', chunk); // 分片二进制数据

      // 上传分片 - 使用正确的API路径
      await axios.post('/api/upload/chunk', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (e) => {
          // 计算当前分片的上传进度，累计到总进度
          const chunkProgress = (e.loaded / e.total) * (100 / totalChunks);
          uploadProgress.value = Math.floor((uploadedChunks * 100) / totalChunks + chunkProgress);
        }
      });
      uploadedChunks++;

      console.log(`分片 ${chunkIndex + 1}/${totalChunks} 上传完成`);
    }

    // 所有分片上传完成，请求合并 - 与后端MergeDTO匹配
    ElMessage.info('所有分片上传完成，正在合并文件...');
    const mergeData = {
      md5: md5,
      fileName: file.name,
      totalChunks: totalChunks
    };

    const mergeRes = await axios.post('/api/upload/merge', mergeData);

    if (mergeRes.data && mergeRes.data.data) {
      uploadProgress.value = 100;
      isUploading.value = false;
      uploadedFileUrl.value = mergeRes.data.data.fileUrl; // 保存返回的文件URL
      duration.value = mergeRes.data.data.duration; // 同步时长（关键：确保duration有值）

      ElMessage.success('视频上传完成');
      console.log('文件上传完成，地址：', uploadedFileUrl.value, '时长：', duration.value);

      // 这里可以处理上传完成后的逻辑
      handleUploadComplete(uploadedFileUrl.value);
    } else {
      throw new Error('文件合并失败');
    }
  } catch (err) {
    console.error('上传失败：', err);
    ElMessage.error('上传失败，请重试');
    isUploading.value = false;
    throw err; // 重新抛出错误，让上层处理
  }
};

// 上传完成后的处理
const handleUploadComplete = (fileUrl) => {
  // 这里可以添加上传完成后的业务逻辑
  // 例如：将文件URL保存到表单数据中，或者触发其他操作
  console.log('上传完成，文件URL:', fileUrl);
};

// 格式化文件大小函数
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};


//==================================================================================================================
//=================================================================================================================


// 核心修改：1. 新增纯对象form（用于表单绑定，避免Proxy问题）
const form = ref({
  title: '',
  category: '',
  type: 'original',
  tags: [],
  description: '',
  coverUrl: ''
});

// 2. 原reactive对象formData（保留响应式逻辑）
const formData = reactive({
  title: '',
  category: '',
  type: 'original',
  tags: [],
  description: '',
  coverUrl: ''
});

// 3. 监听formData变化，同步更新纯对象form（解包Proxy）
watch(
    formData,
    (newVal) => {
      form.value = JSON.parse(JSON.stringify(newVal));
      localStorage.setItem('videoDraft', JSON.stringify(newVal)); // 同步草稿
    },
    { deep: true }
);

// 封面相关
const coverOptions = ref([
  { url: 'https://picsum.photos/300/170?random=1' },
  { url: 'https://picsum.photos/300/170?random=2' },
  { url: 'https://picsum.photos/300/170?random=3' }
]);
const selectedCoverIndex = ref(0);
const selectedCoverUrl = ref(coverOptions.value[0].url); // 封面URL

// 推荐标签
const recommendTags = [
  '热门', '原创', '生活', '教程', 'vlog',
  '搞笑', '美食', '旅行', '音乐', '游戏'
];

// 表单校验规则（不变）
// 表单校验规则
const formRules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: ['blur', 'change'] },
    { min: 2, max: 80, message: '标题长度在 2-80 个字符之间', trigger: ['blur', 'change'] }
  ],
  category: [
    { required: true, message: '请选择内容分类', trigger: ['blur', 'change'] }
  ],
  tags: [
    {
      required: true,
      message: '请至少添加一个标签',
      trigger: ['change', 'blur'],
      validator: (rule, value, callback) => {
        // 手动处理 Proxy 数组，获取真实长度
        const realTags = Array.isArray(value) ? value : [];
        if (realTags.length === 0) {
          callback(new Error('请至少添加一个标签'));
        } else if (realTags.length > 10) {
          callback(new Error('最多添加 10 个标签'));
        } else {
          callback();
        }
      }
    }
  ],
  type: [
    { required: true, message: '请选择创作类型', trigger: 'change' }
  ]
};

// 初始化草稿（同步到form和formData）
const initDraft = () => {
  const draft = localStorage.getItem('videoDraft');
  if (draft) {
    const parsedDraft = JSON.parse(draft);
    parsedDraft.tags = Array.isArray(parsedDraft.tags) ? parsedDraft.tags : []; // 强制数组
    Object.assign(formData, parsedDraft); // 更新Proxy对象
    form.value = JSON.parse(JSON.stringify(parsedDraft)); // 更新纯对象
  }
};
initDraft();


// ===========================================================封面选择----------------------------------------------------
const selectCover = (index) => {
  selectedCoverIndex.value = index;
  selectedCoverUrl.value = coverOptions.value[index].url;
  formData.coverUrl = selectedCoverUrl.value; // 更新Proxy对象（自动同步到form）
};

// 自定义封面上传
const handleCoverUpload = (e) => {
  const file = e.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (event) => {
      coverOptions.value.push({ url: event.target.result });
      selectCover(coverOptions.value.length - 1);
    };
    reader.readAsDataURL(file);
  }
};

// 标签管理（操作formData，自动同步到form）
const addTag = (tag) => {
  const tagValue = tag ? tag.trim() : newTag.value.trim();
  if (tagValue && tagValue.length > 0 && !formData.tags.includes(tagValue) && formData.tags.length < 10) {
    formData.tags.push(tagValue); // 更新Proxy对象的tags
  }
  newTag.value = '';
  showTagInput.value = false;
};

const removeTag = (index) => {
  formData.tags.splice(index, 1); // 更新Proxy对象的tags
};

// 显示标签输入框并聚焦
watch(showTagInput, (value) => {
  if (value) {
    nextTick(() => {
      tagInput.value?.focus();
    });
  }
});


//-====================================================== 表单提交 =========================================================================

import {useUserStore} from "@/store/user.ts";
const user = useUserStore();

// 重置表单
const resetForm = () => {
  formData.title = '';
  formData.category = '';
  formData.type = 'original';
  formData.tags = [];
  formData.description = '';
  formData.coverUrl = '';
  selectedCoverIndex.value = 0;
  selectedCoverUrl.value = coverOptions.value[0].url;
  videoFile.value = null;
  videoUrl.value = '';
  uploadedFileUrl.value = '';
  duration.value = '';
};

const handleSubmit = async () => {
  // 1. 登录校验
  if (!user.userInfo || !user.userInfo.id) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }

  // 2. 视频上传状态校验
  if (!videoFile.value) {
    ElMessage.error('请先上传视频');
    return;
  }
  if (!uploadedFileUrl.value) {
    ElMessage.error('请等待视频上传完成');
    return;
  }

  // 3. 手动兜底校验（确保必填项满足，避免校验误判）
  let hasError = false;
  if (!form.value.title.trim() || form.value.title.length < 2) {
    ElMessage.error('请输入2-80字的视频标题');
    hasError = true;
  }
  if (!form.value.category.trim()) {
    ElMessage.error('请选择内容分类');
    hasError = true;
  }
  if (!Array.isArray(form.value.tags) || form.value.tags.length === 0) {
    ElMessage.error('请至少添加一个标签');
    hasError = true;
  }
  if (hasError) return;

  // 4. Element Plus表单校验（使用纯对象form）
  try {
    await contributionForm.value.validate();
  } catch (err) {
    console.error('表单校验失败详情：', err);
    ElMessage.warning('请完善必填项后提交');
    return;
  }

  // 5. 构建提交参数（完全匹配后端DTO）
  const submitData = {
    userId: Number(user.userInfo.id),
    title: form.value.title.trim(),
    category: form.value.category.trim(),
    type: form.value.type,
    tags: form.value.tags.join(','),
    description: form.value.description || '',
    coverUrl: form.value.coverUrl || selectedCoverUrl.value,
    videoUrl: uploadedFileUrl.value,
    duration: Math.floor(Number(duration.value) || 0)
  };
  console.log('提交参数：', submitData);

  // 6. 发布确认 + 接口请求
  try {
    await ElMessageBox.confirm(
        '确认发布此视频吗？发布后将公开可见',
        '发布确认',
        { confirmButtonText: '确认发布', cancelButtonText: '取消', type: 'info' }
    );

    const response = await axios.post(
        '/api/people/contribution',
        submitData,
        {
          headers: { 'Content-Type': 'application/json' },
          timeout: 30000,
          validateStatus: (status) => true // 接收所有状态码
        }
    );

    console.log('接口状态码:', response.status);
    console.log('接口响应体:', response.data);

    // 关键修改：后端 code=1 表示成功（根据后端日志适配）
    const isSuccess = response.data.code === 1 && response.data.data !== null;

    if (isSuccess) {
      ElMessage.success('视频发布成功！');
      localStorage.removeItem('videoDraft');
      resetForm();
      setTimeout(() => router.push('/profile/videos'), 1500);
    } else {
      // 提取后端错误信息
      const errorMsg =
          response.data.msg ||
          response.data.message ||
          '发布失败（后端返回 code=' + response.data.code + '）';
      throw new Error(errorMsg);
    }
  } catch (error) {
    if (error === 'cancel') return;
    console.error('发布失败详情:', error);
    let errorMsg = '发布失败';
    if (error.message.includes('timeout')) {
      errorMsg = '发布超时，请稍后重试';
    } else if (error.response?.status === 403) {
      errorMsg = '无发布权限，请登录后重试';
    } else if (error.response?.status === 404) {
      errorMsg = '接口不存在，请检查配置';
    } else if (error.response?.status === 500) {
      errorMsg = '服务器异常，请联系管理员';
    } else if (error.message) {
      errorMsg = '发布失败：' + error.message;
    }
    ElMessage.error(errorMsg);
  }
};

// ==========================================================保存草稿 ==========================================================

// 保存草稿
const handleSaveDraft = async () => {
  try {
    const draftData = {
      ...formData,
      videoFile: videoFile.value ? {
        name: videoFile.value.name,
        size: videoFile.value.size,
        type: videoFile.value.type
      } : null,
      uploadedFileUrl: uploadedFileUrl.value,
      selectedCoverUrl: selectedCoverUrl.value,
      duration: duration.value
    };

    const response = await axios.post('/api/video/draft/save', draftData);

    if (response.data && response.data.success) {
      ElMessage.success('草稿保存成功');
      localStorage.setItem('videoDraft', JSON.stringify(draftData));
    } else {
      throw new Error('草稿保存失败');
    }
  } catch (error) {
    console.error('保存草稿失败:', error);
    const localDraft = {
      ...formData,
      videoFile: videoFile.value ? {
        name: videoFile.value.name,
        size: videoFile.value.size,
        type: videoFile.value.type
      } : null,
      uploadedFileUrl: uploadedFileUrl.value,
      selectedCoverUrl: selectedCoverUrl.value,
      duration: duration.value
    };
    localStorage.setItem('videoDraft', JSON.stringify(localDraft));
    ElMessage.success('草稿已保存到本地');
  }
};

// 返回上一页
const handleBack = () => {
  router.back();
};
</script>

<style scoped>
.contribution-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
}

.header-content {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-btn {
  margin-right: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.main-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.upload-section {
  margin-bottom: 20px;
}

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
}

.upload-area:hover {
  border-color: #409eff;
}

.upload-area.uploaded {
  border-style: solid;
  border-color: #67c23a;
}

.file-input {
  display: none;
}

.upload-placeholder .upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 14px;
  color: #909399;
}

.video-preview {
  display: flex;
  align-items: center;
  gap: 20px;
}

.preview-video {
  width: 300px;
  height: 170px;
  border-radius: 4px;
  background-color: #000;
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

.edit-section {
  margin-bottom: 20px;
}

.contribution-form {
  padding: 20px;
}

.form-item {
  margin-bottom: 24px;
}

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

.cover-item.active {
  border-color: #409eff;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

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

.cover-upload:hover {
  border-color: #409eff;
}

.cover-input {
  display: none;
}

.title-input,
.category-select,
.description-input {
  width: 100%;
}

.type-radio {
  display: flex;
  gap: 20px;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}

.tag-item {
  margin-right: 8px;
}

.tag-input {
  width: 120px;
}

.add-tag-btn {
  color: #409eff;
}

.recommend-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.recommend-label {
  font-size: 14px;
  color: #909399;
}

.recommend-tag {
  color: #409eff;
  padding: 0 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 32px;
}

.submit-btn {
  min-width: 120px;
}

.draft-btn {
  min-width: 120px;
}
</style>
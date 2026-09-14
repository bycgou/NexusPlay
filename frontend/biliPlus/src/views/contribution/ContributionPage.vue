<!-- src/views/contribution/ContributionPage.vue -->
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
        <VideoUploader
            v-model="uploadedFileUrl"
            v-model:duration="duration"
            @upload-start="isUploading = true"
            @upload-complete="isUploading = false"
        />
      </el-card>

      <!-- 内容编辑区域 -->
      <el-card class="edit-section">
        <ContributionForm
            ref="contributionForm"
            v-model="form"
            :rules="formRules"
            :recommend-tags="recommendTags"
            :is-uploading="isUploading"
        >
          <template #cover-selector>
            <CoverSelector v-model="form.coverUrl" />
          </template>
        </ContributionForm>

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
      </el-card>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import router from '@/router/index.js';
import axios from 'axios';

// 子组件
import VideoUploader from './components/VideoUploader.vue';
import CoverSelector from './components/CoverSelector.vue';
import ContributionForm from './components/ContributionForm.vue';

// 图标
import { ArrowLeft } from '@element-plus/icons-vue';

// 状态
const uploadedFileUrl = ref('');
const duration = ref(0);
const isUploading = ref(false);
const contributionForm = ref(null);

// 分类名称到ID的映射（与数据库category表对应）
const categoryMap = {
  '生活': 1,
  '游戏': 2,
  '音乐': 3,
  '科技': 4,
  '动画': 5,
  '娱乐': 6,
  '美食': 7,
  '旅行': 8,
  '教程': 9,
  '原创': 10,
  '热门': 11
};

// 表单数据（纯对象，用于 el-form）
const form = ref({
  title: '',
  category: '',
  type: 'original',
  tags: [],
  description: '',
  coverUrl: ''
});

// 响应式 formData（用于 localStorage 同步）
const formData = reactive({
  title: '',
  category: '',
  type: 'original',
  tags: [],
  description: '',
  coverUrl: ''
});

// 同步 formData ↔ form + localStorage
watch(
    formData,
    (newVal) => {
      form.value = JSON.parse(JSON.stringify(newVal));
      localStorage.setItem('videoDraft', JSON.stringify(newVal));
    },
    { deep: true }
);

// 推荐标签（传递给子组件）
const recommendTags = [
  '热门', '原创', '生活', '教程', 'vlog',
  '搞笑', '美食', '旅行', '音乐', '游戏'
];

// 表单规则
const formRules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: ['blur', 'change'] },
    { min: 2, max: 80, message: '标题长度在 2-80 个字符之间', trigger: ['blur', 'change'] }
  ],
  category: [{ required: true, message: '请选择内容分类', trigger: ['blur', 'change'] }],
  tags: [{
    required: true,
    validator: (rule, value, callback) => {
      const realTags = Array.isArray(value) ? value : [];
      if (realTags.length === 0) callback(new Error('请至少添加一个标签'));
      else if (realTags.length > 10) callback(new Error('最多添加 10 个标签'));
      else callback();
    },
    trigger: ['change', 'blur']
  }],
  type: [{ required: true, message: '请选择创作类型', trigger: 'change' }]
};

// 初始化草稿
const initDraft = () => {
  const draft = localStorage.getItem('videoDraft');
  if (draft) {
    const parsed = JSON.parse(draft);
    parsed.tags = Array.isArray(parsed.tags) ? parsed.tags : [];
    Object.assign(formData, parsed);
    form.value = JSON.parse(JSON.stringify(parsed));
  }
};
onMounted(() => {
  initDraft();
});

// 用户状态
import { useUserStore } from '@/store/user.js';
import {submitContribution} from "@/api/contribution.js";
const user = useUserStore();

// 返回
const handleBack = () => router.back();

// 重置
const resetForm = () => {
  Object.assign(formData, {
    title: '',
    category: '',
    type: 'original',
    tags: [],
    description: '',
    coverUrl: ''
  });
  uploadedFileUrl.value = '';
  duration.value = 0;
  localStorage.removeItem('videoDraft');
};

// 提交
const handleSubmit = async () => {
  if (!user.userInfo?.id) {
    ElMessage.error('请先登录');
    return router.push('/login');
  }
  if (!uploadedFileUrl.value) {
    ElMessage.error('请等待视频上传完成');
    return;
  }



  try {
    await contributionForm.value.validate();
  } catch {
    return ElMessage.warning('请完善必填项后提交');
  }

  // 根据分类名称获取分类ID，默认为10（原创）
  const categoryId = categoryMap[form.value.category] || 10;

  const submitData = {
    userId: Number(user.userInfo.id),
    title: form.value.title.trim(),
    category: form.value.category,
    categoryId: categoryId,
    type: form.value.type,
    tags: form.value.tags.join(','),
    description: form.value.description || '',
    coverUrl: form.value.coverUrl || '',
    videoUrl: uploadedFileUrl.value,
    duration: Math.floor(Number(duration.value) || 0)
  };

  try {
    await ElMessageBox.confirm('确认发布此视频吗？发布后将公开可见', '发布确认');
    const res = await submitContribution(submitData);

    if (res.code === 1 && res.data) {
      ElMessage.success('视频发布成功！');
      resetForm();
      setTimeout(() => router.push('/'), 1500);
    } else {
      throw new Error(res.data.msg || '发布失败');
    }
  } catch (error) {
    if (error === 'cancel') return;
    let msg = '发布失败';
    if (error.message.includes('timeout')) msg = '发布超时，请稍后重试';
    else if (error.response?.status === 403) msg = '无发布权限，请登录后重试';
    else if (error.response?.status === 500) msg = '服务器异常，请联系管理员';
    else if (error.message) msg = '发布失败：' + error.message;
    ElMessage.error(msg);
  }
};

// 保存草稿
const handleSaveDraft = async () => {
  const draftData = {
    ...formData,
    uploadedFileUrl: uploadedFileUrl.value,
    duration: duration.value
  };
  try {
    const res = await axios.post('/api/video/draft/save', draftData);
    if (res.data?.success) {
      ElMessage.success('草稿保存成功');
    } else throw new Error();
  } catch {
    localStorage.setItem('videoDraft', JSON.stringify(draftData));
    ElMessage.success('草稿已保存到本地');
  }
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
.edit-section {
  margin-bottom: 20px;
}
.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 32px;
}
.submit-btn, .draft-btn {
  min-width: 120px;
}
</style>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMyVideos, updateMyVideo, deleteMyVideo, resubmitMyVideo } from '@/api/myVideo'
import { getCategories } from '@/api/category'
import ImageUploader from '@/components/ImageUploader.vue'
import TagEditor from '@/views/contribution/components/TagEditor.vue'

interface MyVideo {
  id: number
  title: string
  description?: string
  coverUrl?: string
  categoryId?: number
  status?: number
  rejectReason?: string
  tags?: string
  createTime?: string
}

const router = useRouter()

const loading = ref(false)
const videos = ref<MyVideo[]>([])

const statusText = (s?: number) => {
  if (s === 0) return '审核中'
  if (s === 1) return '已通过'
  if (s === 2) return '已下架'
  if (s === 3) return '未通过'
  return '未知'
}

const statusType = (s?: number) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  if (s === 2) return 'info'
  if (s === 3) return 'danger'
  return 'info'
}

const canResubmit = (s?: number) => s === 2 || s === 3

const loadVideos = async () => {
  loading.value = true
  try {
    const res = await getMyVideos()
    if (res.code === 1) {
      videos.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// ===== 分类下拉 =====

const categories = ref<{ id: number; name: string }[]>([])

const loadCategories = async () => {
  try {
    const res = await getCategories(1)
    if (res.code === 1 && Array.isArray(res.data)) {
      categories.value = res.data
    }
  } catch (e) {
    console.warn('加载分类失败', e)
  }
}

// ===== 编辑弹窗 =====

const editVisible = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  title: '',
  description: '',
  categoryId: undefined as number | undefined,
  coverUrl: '',
  tags: [] as string[]
})

const dialogTitle = computed(() => `编辑稿件 #${editingId.value ?? ''}`)

const recommendTags = ['原创', 'VLOG', '游戏', '音乐', '科技', '生活', '学习', '鬼畜']

const openEdit = (item: MyVideo) => {
  editingId.value = item.id
  form.value = {
    title: item.title || '',
    description: item.description || '',
    categoryId: item.categoryId,
    coverUrl: item.coverUrl || '',
    // 后端以逗号分隔字符串返回
    tags: item.tags ? item.tags.split(',').filter((t) => t.trim()).map((t) => t.trim()) : []
  }
  editVisible.value = true
}

const submitEdit = async () => {
  if (editingId.value == null) return

  const title = form.value.title.trim()
  if (!title) {
    ElMessage.warning('请填写视频标题')
    return
  }
  if (title.length > 100) {
    ElMessage.warning('标题不能超过100字')
    return
  }

  submitting.value = true
  try {
    const res = await updateMyVideo(editingId.value, {
      title,
      description: form.value.description,
      categoryId: form.value.categoryId,
      coverUrl: form.value.coverUrl,
      tags: form.value.tags.join(',')
    })
    if (res.code === 1) {
      ElMessage.success('修改已保存')
      editVisible.value = false
      await loadVideos()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    console.error('编辑稿件失败', e)
  } finally {
    submitting.value = false
  }
}

// ===== 重提 / 删除 =====

const handleResubmit = async (item: MyVideo) => {
  try {
    const res = await resubmitMyVideo(item.id)
    if (res.code === 1) {
      ElMessage.success('已重新提交审核')
      await loadVideos()
    } else {
      ElMessage.error(res.msg || '重新提交失败')
    }
  } catch (e) {
    console.error('重新提交失败', e)
  }
}

const handleDelete = async (item: MyVideo) => {
  try {
    await ElMessageBox.confirm(
      `删除后《${item.title}》将不再展示，且无法在用户端恢复。确定删除吗？`,
      '删除稿件',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    // 用户取消
    return
  }
  try {
    const res = await deleteMyVideo(item.id)
    if (res.code === 1) {
      ElMessage.success('稿件已删除')
      await loadVideos()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    console.error('删除稿件失败', e)
  }
}

const goWatch = (item: MyVideo) => {
  router.push(`/video/${item.id}`)
}

onMounted(() => {
  loadVideos()
  loadCategories()
})
</script>

<template>
  <div class="my-videos">
    <div class="head">
      <h3>我的稿件</h3>
      <el-button size="small" @click="loadVideos">刷新</el-button>
    </div>

    <el-empty v-if="!loading && videos.length === 0" description="还没有投稿" />

    <div v-else class="list" v-loading="loading">
      <div v-for="item in videos" :key="item.id" class="card">
        <img :src="item.coverUrl" class="cover" alt="" />
        <div class="info">
          <div class="title-row">
            <span class="title">{{ item.title }}</span>
            <el-tag size="small" :type="statusType(item.status)">{{ statusText(item.status) }}</el-tag>
          </div>
          <div class="meta">
            投稿时间：{{ item.createTime || '—' }}
            <template v-if="item.tags"> · 标签：{{ item.tags }}</template>
          </div>
          <el-alert
              v-if="item.status === 3 && item.rejectReason"
              type="error"
              :closable="false"
              show-icon
              class="reject"
          >
            <template #title>审核未通过</template>
            {{ item.rejectReason }}
          </el-alert>
          <div v-else-if="item.status === 0" class="tip">视频正在审核中，请耐心等待</div>
          <div v-else-if="item.status === 1" class="tip ok">视频已通过审核并展示</div>
          <div v-else-if="item.status === 2" class="tip">视频已下架，可修改后重新提交</div>

          <div class="actions">
            <el-button size="small" @click="openEdit(item)">编辑</el-button>
            <el-button
                v-if="canResubmit(item.status)"
                size="small"
                type="primary"
                @click="handleResubmit(item)"
            >
              重新提交
            </el-button>
            <el-button
                v-if="item.status === 1"
                size="small"
                @click="goWatch(item)"
            >
              去观看
            </el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="editVisible" :title="dialogTitle" width="620px">
      <el-form label-width="72px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              maxlength="2000"
              show-word-limit
              placeholder="介绍一下这个视频"
          />
        </el-form-item>
        <el-form-item label="分区">
          <el-select v-model="form.categoryId" placeholder="请选择分区" style="width: 220px">
            <el-option
                v-for="c in categories"
                :key="c.id"
                :label="c.name"
                :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="封面">
          <ImageUploader v-model="form.coverUrl" tip="点击上传封面" />
        </el-form-item>
        <el-form-item label="标签">
          <TagEditor v-model="form.tags" :recommend-tags="recommendTags" />
        </el-form-item>
        <el-alert
            type="info"
            :closable="false"
            title="本期不支持更换视频源文件；如需换源请重新投稿。被驳回或已下架的稿件保存后会重新进入待审。"
        />
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.my-videos {
  padding: 8px 4px;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.head h3 {
  margin: 0;
  font-size: 16px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.card {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fff;
}
.cover {
  width: 140px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  background: #f5f7fa;
  flex-shrink: 0;
}
.info {
  flex: 1;
  min-width: 0;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}
.reject {
  border-radius: 6px;
}
.tip {
  font-size: 12px;
  color: #909399;
}
.tip.ok {
  color: #67c23a;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
.actions :deep(.el-button + .el-button) {
  margin-left: 0;
}
</style>

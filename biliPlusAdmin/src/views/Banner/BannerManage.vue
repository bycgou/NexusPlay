<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getBannerList,
  createBanner,
  updateBanner,
  deleteBanner,
  type Banner
} from '@/api/banner'

const loading = ref(false)
const list = ref<Banner[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增轮播图')
const editingId = ref<number | null>(null)

const emptyForm = (): Banner => ({
  title: '',
  description: '',
  imageUrl: '',
  linkType: 1,
  videoId: null,
  linkUrl: null,
  sortOrder: 0,
  status: 1,
})

const form = ref<Banner>(emptyForm())

const linkTypeLabel = (t?: number) => {
  if (t === 1) return '视频'
  if (t === 2) return '外链'
  return '不跳转'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getBannerList()
    list.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新增轮播图'
  editingId.value = null
  form.value = emptyForm()
  dialogVisible.value = true
}

const openEdit = (row: Banner) => {
  dialogTitle.value = '编辑轮播图'
  editingId.value = row.id!
  form.value = {
    title: row.title,
    description: row.description || '',
    imageUrl: row.imageUrl,
    linkType: row.linkType ?? 3,
    videoId: row.videoId ?? null,
    linkUrl: row.linkUrl ?? null,
    sortOrder: row.sortOrder ?? 0,
    status: row.status ?? 1,
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  if (!form.value.imageUrl?.trim()) {
    ElMessage.warning('请填写图片地址')
    return
  }
  if (form.value.linkType === 1 && !form.value.videoId) {
    ElMessage.warning('跳转视频时请填写视频ID')
    return
  }
  try {
    if (editingId.value) {
      await updateBanner(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createBanner(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row: Banner) => {
  try {
    await ElMessageBox.confirm(`确认删除「${row.title}」？`, '提示', { type: 'warning' })
    await deleteBanner(row.id!)
    ElMessage.success('已删除')
    await loadList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleUploadSuccess = (url: string) => {
  form.value.imageUrl = url
}

onMounted(loadList)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增轮播图</el-button>
      <el-button @click="loadList">刷新</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="预览" width="160">
        <template #default="scope">
          <el-image
              v-if="scope.row.imageUrl"
              :src="scope.row.imageUrl"
              style="width: 140px; height: 60px; object-fit: cover; border-radius: 6px;"
              fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="140" show-overflow-tooltip />
      <el-table-column label="跳转" width="100">
        <template #default="scope">
          <el-tag size="small">{{ linkTypeLabel(scope.row.linkType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="目标" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.linkType === 1">视频 #{{ scope.row.videoId }}</span>
          <span v-else-if="scope.row.linkType === 2">{{ scope.row.linkUrl }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
            {{ scope.row.status === 1 ? '上线' : '下线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="100" placeholder="轮播图标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" maxlength="255" placeholder="副标题/描述，可空" />
        </el-form-item>
        <el-form-item label="图片URL" required>
          <el-input v-model="form.imageUrl" placeholder="如 /longshu-banner.jpg 或完整 http 地址" />
          <div class="tip">可先用「投稿」或封面上传得到图片地址后粘贴；也可填站内 public 路径</div>
        </el-form-item>
        <el-form-item label="跳转类型">
          <el-radio-group v-model="form.linkType">
            <el-radio :value="1">视频</el-radio>
            <el-radio :value="2">外链</el-radio>
            <el-radio :value="3">不跳转</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.linkType === 1" label="视频ID" required>
          <el-input-number v-model="form.videoId" :min="1" placeholder="视频ID" />
        </el-form-item>
        <el-form-item v-if="form.linkType === 2" label="外链">
          <el-input v-model="form.linkUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
          <span class="tip-inline">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上线" inactive-text="下线" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 16px 20px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
.tip-inline {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>

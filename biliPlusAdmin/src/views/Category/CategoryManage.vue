<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoryList,
  createCategory,
  updateCategory,
  deleteCategory,
  type Category
} from '@/api/category'
import ImageUpload from '@/components/ImageUpload.vue'

const loading = ref(false)
const list = ref<Category[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const form = ref<Category>({ name: '', parentId: 0, sortOrder: 0, icon: '', type: 1 })
const editingId = ref<number | null>(null)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getCategoryList(1)
    list.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新增分类'
  editingId.value = null
  form.value = { name: '', parentId: 0, sortOrder: 0, icon: '', type: 1 }
  dialogVisible.value = true
}

const openEdit = (row: Category) => {
  dialogTitle.value = '编辑分类'
  editingId.value = row.id!
  form.value = {
    name: row.name,
    parentId: row.parentId ?? 0,
    sortOrder: row.sortOrder ?? 0,
    icon: row.icon || '',
    type: row.type ?? 1
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    const payload = { ...form.value, type: form.value.type ?? 1 }
    if (editingId.value) {
      await updateCategory(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createCategory(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row: Category) => {
  try {
    await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '提示', { type: 'warning' })
    await deleteCategory(row.id!)
    ElMessage.success('删除成功')
    await loadList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(loadList)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增分类</el-button>
      <el-button @click="loadList">刷新</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="parentId" label="父分类ID" width="110" />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column prop="icon" label="图标" width="90">
        <template #default="scope">
          <el-image
              v-if="scope.row.icon"
              :src="scope.row.icon"
              style="width: 36px; height: 36px; border-radius: 4px;"
              fit="cover"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="父分类ID">
          <el-input-number v-model="form.parentId" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="图标">
          <ImageUpload v-model="form.icon" tip="上传图标（可选）" />
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
  padding: 12px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
</style>

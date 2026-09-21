<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGiftList, createGift, updateGift, deleteGift, type Gift } from '@/api/live'
import ImageUpload from '@/components/ImageUpload.vue'

const loading = ref(false)
const list = ref<Gift[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增礼物')
const editingId = ref<number | null>(null)

const emptyForm = (): Gift => ({
  name: '',
  iconUrl: '',
  price: 10,
  effectLevel: 1,
  sortOrder: 0,
  status: 1
})

const form = ref<Gift>(emptyForm())

const loadList = async () => {
  loading.value = true
  try {
    const res = await getGiftList()
    list.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新增礼物'
  editingId.value = null
  form.value = emptyForm()
  dialogVisible.value = true
}

const openEdit = (row: Gift) => {
  dialogTitle.value = '编辑礼物'
  editingId.value = row.id!
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请填写名称')
    return
  }
  if (!form.value.iconUrl?.trim()) {
    ElMessage.warning('请填写图标地址')
    return
  }
  if (!form.value.price || form.value.price <= 0) {
    ElMessage.warning('价格必须大于0')
    return
  }
  try {
    if (editingId.value) {
      await updateGift(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createGift(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row: Gift) => {
  try {
    await ElMessageBox.confirm(`确认删除「${row.name}」？`, '提示', { type: 'warning' })
    await deleteGift(row.id!)
    ElMessage.success('已删除')
    await loadList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const levelText = (lv?: number) => {
  if (lv === 3) return '全屏'
  if (lv === 2) return '中等'
  return '普通'
}

onMounted(loadList)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增礼物</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="图标" width="80">
        <template #default="{ row }">
          <img v-if="row.iconUrl" :src="row.iconUrl" class="icon" alt="" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="120" />
      <el-table-column prop="price" label="价格" width="90" />
      <el-table-column label="特效" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ levelText(row.effectLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="图标" required>
          <ImageUpload v-model="form.iconUrl" tip="上传礼物图标" accept="image/*" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="1" />
        </el-form-item>
        <el-form-item label="特效等级">
          <el-select v-model="form.effectLevel" style="width: 100%">
            <el-option label="普通" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="全屏" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 12px; }
.icon { width: 32px; height: 32px; object-fit: contain; }
</style>

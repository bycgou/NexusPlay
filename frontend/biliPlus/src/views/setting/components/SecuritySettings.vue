<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '新密码长度不能少于8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (err?: Error) => void) => {
        if (value !== form.newPassword) callback(new Error('两次输入的新密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const formRef = ref()

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    if (res.code === 1) {
      ElMessage.success('密码修改成功，请重新登录')
      form.oldPassword = ''
      form.newPassword = ''
      form.confirmPassword = ''
      formRef.value?.resetFields()
    } else {
      ElMessage.error(res.msg || '密码修改失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '密码修改失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="settings-panel">
    <h3 class="panel-title">账号安全</h3>
    <p class="panel-desc">管理登录邮箱、密码与账号状态</p>

    <el-card shadow="never" class="info-card">
      <div class="info-row">
        <span class="label">登录邮箱</span>
        <span class="value">{{ userStore.userInfo.email || '未绑定' }}</span>
      </div>
      <div class="info-row">
        <span class="label">用户名</span>
        <span class="value">{{ userStore.userInfo.username || '—' }}</span>
      </div>
      <div class="info-row">
        <span class="label">用户 ID</span>
        <span class="value">{{ userStore.userInfo.id || '—' }}</span>
      </div>
      <div class="info-row">
        <span class="label">账号状态</span>
        <el-tag type="success" size="small">正常</el-tag>
      </div>
    </el-card>

    <h4 class="section-title">修改密码</h4>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="settings-form"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          show-password
          placeholder="请输入当前密码"
          autocomplete="current-password"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          show-password
          placeholder="至少 8 位"
          autocomplete="new-password"
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
          autocomplete="new-password"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">确认修改</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip-alert"
      title="安全提示"
      description="修改密码成功后需要重新登录。请勿向他人泄露密码，建议定期更换。"
    />
  </div>
</template>

<style scoped>
@import './settings-common.css';

.info-card {
  margin-bottom: 24px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--paper-white);
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--line-light);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: var(--mist);
  font-size: 14px;
}

.info-row .value {
  color: var(--ink);
  font-weight: 500;
}

.section-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
}

.tip-alert {
  margin-top: 16px;
  border-radius: var(--radius-md);
}
</style>

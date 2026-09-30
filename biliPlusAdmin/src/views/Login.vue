<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const username = ref('')
const password = ref('')
const loading = ref(false)

const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  if (!username.value.trim()) {
    ElMessage.warning('请输入账号')
    return
  }
  if (!password.value.trim()) {
    ElMessage.warning('请输入密码')
    return
  }

  try {
    loading.value = true
    const res = await login({
      account: username.value.trim(),
      password: password.value.trim()
    })
    authStore.login(res.data.token, {
      id: String(res.data.id),
      name: res.data.name || res.data.account
    })
    ElMessage.success('登录成功')
    await router.push('/Home')
  } catch (error: any) {
    console.error('登录失败详情：', error)
    if (typeof error === 'object' && error !== null && 'msg' in error) {
      ElMessage.error(error.msg || '登录失败，请重试')
    } else {
      ElMessage.error('网络异常或服务器错误')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="brand-row">
        <div class="brand-logo">N</div>
        <div>
          <h2 class="title">NexusPlay 管理后台</h2>
          <p class="sub">内容治理 · 用户运营 · 资金对账</p>
        </div>
      </div>
      <el-col>
        <el-input
            v-model="username"
            placeholder="请输入管理员账号"
            class="input-item"
            clearable
            @keyup.enter="handleLogin"
        />
        <el-input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            class="input-item"
            show-password
            clearable
            @keyup.enter="handleLogin"
        />
        <el-button
            type="primary"
            @click="handleLogin"
            class="login-btn"
            :loading="loading"
        >
          登录
        </el-button>
      </el-col>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(1000px 520px at 12% -8%, rgba(37, 99, 235, 0.16), transparent 60%),
    radial-gradient(800px 480px at 108% 108%, rgba(96, 165, 250, 0.18), transparent 55%),
    #f0f6ff;
}
.login-container {
  width: 380px;
  padding: 32px 28px;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(37, 99, 235, 0.12);
  background: #fff;
  border: 1px solid #e8eef7;
}
.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 22px;
}
.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563EB 0%, #60A5FA 100%);
  color: #fff;
  font-weight: 700;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.title {
  margin: 0;
  font-size: 18px;
  color: #1e293b;
}
.sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #94a3b8;
}
.input-item {
  width: 100%;
  margin-bottom: 15px;
}
.login-btn {
  width: 100%;
}
</style>

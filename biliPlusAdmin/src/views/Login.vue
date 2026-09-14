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
  <div class="login-container">
    <h2 class="title">BiliPlus 管理后台</h2>
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
</template>

<style scoped>
.login-container {
  width: 360px;
  margin: 100px auto;
  padding: 28px 24px;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  background: #fff;
}
.title {
  text-align: center;
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}
.input-item {
  width: 100%;
  margin-bottom: 15px;
}
.login-btn {
  width: 100%;
}
</style>

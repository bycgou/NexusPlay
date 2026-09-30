<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useUserStore } from '@/store/user'
import { authPrompt } from '@/composables/useAuthPrompt'

const userStore = useUserStore()

const mode = ref<'login' | 'register'>('login')
const loginFormRef = ref()
const registerFormRef = ref()
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive({ email: '', password: '' })
const registerForm = reactive({
    email: '',
    password: '',
    confirmPassword: '',
    imageCaptcha: '',
    emailCaptcha: ''
})

const loginRules = reactive({
    email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
    ],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const registerRules = reactive({
    email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
            validator: (_rule: any, value: string, callback: any) => {
                if (value !== registerForm.password) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],
    imageCaptcha: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }],
    emailCaptcha: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }]
})

watch(
    () => [authPrompt.visible, authPrompt.mode] as const,
    ([visible, m]) => {
        if (visible) {
            mode.value = m
        }
    },
    { immediate: true }
)

const handleClose = () => {
    authPrompt.close()
}

const switchMode = (m: 'login' | 'register') => {
    mode.value = m
    authPrompt.mode = m
}

const handleLogin = async () => {
    if (!loginFormRef.value) return
    try {
        await loginFormRef.value.validate()
    } catch {
        return
    }
    loginLoading.value = true
    try {
        const res = await axios.post('/api/pp/people/login', {
            email: loginForm.email,
            password: loginForm.password
        })
        const body = res.data
        if (body?.code !== 1) {
            ElMessage.error(body?.msg || '登录失败')
            return
        }
        if (!body?.data?.token) {
            ElMessage.error('登录失败：未获取到有效 token')
            return
        }
        userStore.login(body.data)
        ElMessage.success('登录成功')
        loginForm.email = ''
        loginForm.password = ''
        authPrompt.close()
        window.location.reload()
    } catch (e: any) {
        ElMessage.error(e?.response?.data?.msg || e?.message || '登录失败，请稍后重试')
    } finally {
        loginLoading.value = false
    }
}

const handleRegister = async () => {
    if (!registerFormRef.value) return
    try {
        await registerFormRef.value.validate()
    } catch {
        return
    }
    registerLoading.value = true
    try {
        const res = await axios.post('/api/pp/people/register', {
            email: registerForm.email,
            password: registerForm.password,
            emailCaptcha: registerForm.emailCaptcha
        })
        const body = res.data
        if (body?.code !== 1) {
            ElMessage.error(body?.msg || '注册失败')
            return
        }
        ElMessage.success('注册成功，请登录')
        registerForm.email = ''
        registerForm.password = ''
        registerForm.confirmPassword = ''
        registerForm.imageCaptcha = ''
        registerForm.emailCaptcha = ''
        switchMode('login')
    } catch (e: any) {
        ElMessage.error(e?.response?.data?.msg || e?.message || '注册失败，请稍后重试')
    } finally {
        registerLoading.value = false
    }
}

const sendEmailCaptcha = async () => {
    if (!registerForm.email) {
        ElMessage.warning('请先输入邮箱')
        return
    }
    try {
        const res = await axios.post('/api/pp/people/email', {
            email: registerForm.email,
            imageCaptcha: registerForm.imageCaptcha
        })
        if (res.data?.code === 1) {
            ElMessage.success('邮箱验证码已发送')
        } else {
            ElMessage.error(res.data?.msg || '发送失败')
        }
    } catch (e: any) {
        ElMessage.error(e?.response?.data?.msg || '发送失败')
    }
}
</script>

<template>
  <el-dialog
      v-model="authPrompt.visible"
      :title="mode === 'login' ? '登录 NexusPlay' : '注册 NexusPlay'"
      width="420px"
      align-center
      @close="handleClose"
  >
    <el-form
        v-if="mode === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        @submit.prevent="handleLogin"
    >
      <el-form-item prop="email">
        <el-input v-model="loginForm.email" placeholder="请输入邮箱" size="large" clearable />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
        />
      </el-form-item>
      <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="loginLoading"
          @click="handleLogin"
      >
        登 录
      </el-button>
      <div class="switch-line">
        还没有账号？
        <el-link type="primary" :underline="false" @click="switchMode('register')">立即注册</el-link>
      </div>
    </el-form>

    <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="0"
    >
      <el-form-item prop="email">
        <el-input v-model="registerForm.email" placeholder="请输入邮箱" size="large" clearable />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20 位）"
            size="large"
            show-password
        />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            show-password
        />
      </el-form-item>
      <el-form-item prop="imageCaptcha">
        <el-input v-model="registerForm.imageCaptcha" placeholder="图形验证码" size="large" />
      </el-form-item>
      <el-form-item prop="emailCaptcha">
        <div class="captcha-row">
          <el-input v-model="registerForm.emailCaptcha" placeholder="邮箱验证码" size="large" />
          <el-button size="large" @click="sendEmailCaptcha">发送验证码</el-button>
        </div>
      </el-form-item>
      <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="registerLoading"
          @click="handleRegister"
      >
        提交注册
      </el-button>
      <div class="switch-line">
        已有账号？
        <el-link type="primary" :underline="false" @click="switchMode('login')">去登录</el-link>
      </div>
    </el-form>
  </el-dialog>
</template>

<style scoped>
.submit-btn {
  width: 100%;
  margin-top: 4px;
}
.switch-line {
  margin-top: 14px;
  text-align: center;
  font-size: 13px;
  color: var(--mist);
}
.captcha-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.captcha-row .el-input {
  flex: 1;
}
</style>

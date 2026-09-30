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
const sendLoading = ref(false)

const loginForm = reactive({ email: '', password: '' })
const registerForm = reactive({
    email: '',
    password: '',
    confirmPassword: '',
    imageCaptcha: '',
    emailCaptcha: ''
})

const captchaImg = ref('')
const captchaId = ref('')

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

// 获取图形验证码（data:image/png;base64,...）
const refreshCaptcha = async () => {
    try {
        const res = await axios.get('/api/pp/people/captcha/image')
        const data = res.data?.data
        if (res.data?.code === 1 && data?.imageBase64) {
            captchaImg.value = data.imageBase64
            captchaId.value = data.captchaId || ''
            registerForm.imageCaptcha = ''
        } else {
            captchaImg.value = ''
            captchaId.value = ''
            ElMessage.error(res.data?.msg || '获取图形验证码失败')
        }
    } catch (e: any) {
        captchaImg.value = ''
        captchaId.value = ''
        ElMessage.error(e?.response?.data?.msg || '获取图形验证码失败，请重试')
    }
}

watch(
    () => [authPrompt.visible, authPrompt.mode] as const,
    ([visible, m]) => {
        if (visible) {
            mode.value = m
            if (m === 'register') {
                refreshCaptcha()
            }
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
    if (m === 'register') {
        refreshCaptcha()
    }
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
    if (!registerForm.imageCaptcha) {
        ElMessage.warning('请输入图形验证码')
        return
    }
    if (!captchaId.value) {
        ElMessage.warning('请先获取图形验证码')
        await refreshCaptcha()
        return
    }
    sendLoading.value = true
    try {
        const res = await axios.post('/api/pp/people/email', {
            email: registerForm.email,
            captchaId: captchaId.value,
            imageCaptcha: registerForm.imageCaptcha
        })
        if (res.data?.code === 1) {
            ElMessage.success('邮箱验证码已发送')
            // 图形验证码一次性，发送后刷新
            await refreshCaptcha()
        } else {
            ElMessage.error(res.data?.msg || '发送失败')
            await refreshCaptcha()
        }
    } catch (e: any) {
        ElMessage.error(e?.response?.data?.msg || '发送失败')
        await refreshCaptcha()
    } finally {
        sendLoading.value = false
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
        <div class="captcha-row">
          <el-input
              v-model="registerForm.imageCaptcha"
              placeholder="图形验证码"
              size="large"
              @keyup.enter="sendEmailCaptcha"
          />
          <div class="captcha-img-box" title="点击刷新" @click="refreshCaptcha">
            <img v-if="captchaImg" :src="captchaImg" alt="图形验证码" class="captcha-img" />
            <span v-else class="captcha-placeholder" @click="refreshCaptcha">点击获取</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item prop="emailCaptcha">
        <div class="captcha-row">
          <el-input v-model="registerForm.emailCaptcha" placeholder="邮箱验证码" size="large" />
          <el-button size="large" :loading="sendLoading" @click="sendEmailCaptcha">发送验证码</el-button>
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
  align-items: center;
}
.captcha-row .el-input {
  flex: 1;
}
.captcha-img-box {
  width: 120px;
  height: 40px;
  flex-shrink: 0;
  border: 1px solid var(--line, #dcdfe6);
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
}
.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.captcha-placeholder {
  font-size: 12px;
  color: var(--mist, #909399);
}
</style>

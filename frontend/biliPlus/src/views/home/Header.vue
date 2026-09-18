<template>
  <!-- 顶部导航栏 -->
  <el-header height="60px" class="header-container">
    <div class="header-content">
      <!-- Logo区域 -->
      <div class="logo" @click="openShouYe">
        <img src="/favicon.ico" alt="logo" class="logo-img" />
        <span class="logo-text">NexusPlay</span>
      </div>

      <!-- 导航菜单 -->
      <el-menu
          mode="horizontal"
          :ellipsis="false"
          :default-active="activeNav"
          background-color="transparent"
          text-color="var(--ink-secondary)"
          active-text-color="var(--brand)"
          class="nav-menu"
          router
      >
        <el-menu-item index="/" class="nav-item">首页</el-menu-item>
        <el-menu-item index="/channel/animation" class="nav-item">动画</el-menu-item>
        <el-menu-item index="/anime" class="nav-item">番剧</el-menu-item>
        <el-menu-item index="/channel/movie" class="nav-item">影视</el-menu-item>
        <el-menu-item index="/live" class="nav-item">直播</el-menu-item>
      </el-menu>

      <!-- 右侧功能区 -->
      <div class="right-actions">
        <!-- 搜索框 -->
        <el-input
            v-model="searchText"
            placeholder="搜索视频、用户..."
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <!-- 主题切换 -->
        <el-tooltip :content="theme === 'dark' ? '切换到亮色模式' : '切换到暗色模式'" placement="bottom">
          <div class="theme-toggle notification-badge" @click="toggleTheme">
            <el-icon size="20" class="action-icon">
              <Moon v-if="theme === 'light'" />
              <Sunny v-else />
            </el-icon>
          </div>
        </el-tooltip>

        <!-- 通知图标（暂无通知系统时不显示红点） -->
        <el-tooltip content="通知" placement="bottom">
          <div class="notification-badge" @click="handleNotificationClick">
            <el-icon size="20" class="action-icon">
              <Bell />
            </el-icon>
          </div>
        </el-tooltip>

        <!-- 私信：有未读才显示红点 -->
        <el-tooltip content="私信" placement="bottom">
          <div class="notification-badge msg-badge" @click="goToMessage">
            <el-icon size="20" class="action-icon">
              <Message />
            </el-icon>
            <span v-if="unreadMsgCount > 0" class="dot-badge">
              {{ unreadMsgCount > 99 ? '99+' : unreadMsgCount }}
            </span>
          </div>
        </el-tooltip>

        <!-- 投稿按钮 -->
        <el-button type="primary" class="submit-btn" @click="openTougao">
          <el-icon><VideoCamera /></el-icon>
          <span>投稿</span>
        </el-button>

        <!-- 用户菜单 -->
        <el-dropdown placement="bottom-end">
          <div class="user-menu">
            <el-avatar class="user-avatar" :src="userStore.userInfo.avatar || '/User.jpg'"></el-avatar>
            <span class="user-name">{{ userStore.userInfo.nickname || userStore.userInfo.username || "登录" }}</span>
            <el-icon size="14" class="arrow-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <!-- 未登录：登录 + 注册 -->
              <template v-if="!userStore.isLogin">
                <el-dropdown-item @click="openLoginDialog">登录</el-dropdown-item>
                <el-dropdown-item @click="openRegisterDialog">注册</el-dropdown-item>
              </template>
              <!-- 已登录：个人主页 + 设置 + 退出 -->
              <template v-else>
                <el-dropdown-item @click="openProfile">
                  <el-icon><User /></el-icon>个人主页
                </el-dropdown-item>
                <el-dropdown-item @click="openSettingDialog">
                  <el-icon><Setting /></el-icon>设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </el-header>




  <!-- 注册弹窗 -->
  <el-dialog
      v-model="registerDialogVisible"
      title="用户注册"
      width="30%"
      :close-on-click-modal="false"
  >
    <!-- 注册表单内容 -->
    <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
    >
      <!-- 邮箱输入 -->
      <el-form-item label="邮箱" prop="email">
        <el-input
            v-model="registerForm.email"
            placeholder="请输入注册邮箱"
            clearable
        ></el-input>
      </el-form-item>

      <!-- 密码输入 -->
      <el-form-item label="密码" prop="password">
        <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少8位）"
            show-password
        ></el-input>
      </el-form-item>

      <!-- 确认密码 -->
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
        ></el-input>
      </el-form-item>

      <!-- 图片验证码 -->
      <el-form-item label="图形验证" prop="imageCaptcha">
        <el-row :gutter="10">
          <el-col :span="14">
            <el-input
                v-model="registerForm.imageCaptcha"
                placeholder="请输入图形验证码"
            ></el-input>
          </el-col>
          <el-col :span="10">
            <div class="captcha-container">
              <img
                  :src="captchaImg"
                  alt="验证码"
                  class="captcha-img"
                  @click="refreshCaptcha"
              >
              <el-button
                  type="text"
                  size="small"
                  @click="refreshCaptcha"
                  class="refresh-btn"
              >
                刷新
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form-item>

      <!-- 邮箱验证码 -->
      <el-form-item label="邮箱验证" prop="emailCaptcha">
        <el-row :gutter="10">
          <el-col :span="14">
            <el-input
                v-model="registerForm.emailCaptcha"
                placeholder="请输入邮箱验证码"
            ></el-input>
          </el-col>
          <el-col :span="10">
            <el-button
                type="primary"
                size="default"
                @click="sendEmailCaptcha"
                :disabled="isSending || countDown > 0"
            >
              {{ countDown > 0 ? `${countDown}s后重发` : '发送验证码' }}
            </el-button>
          </el-col>
        </el-row>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleRegister">提交注册</el-button>
        <el-button @click="resetRegisterForm">重置</el-button>
        <el-button @click="registerDialogVisible = false">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>



  <!-- 登录弹窗 -->
  <el-dialog
      v-model="dialogVisible"
      title="用户登录"
      width="30%"
      :close-on-click-modal="false"
  >
    <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
    >
      <el-form-item label="用户邮箱" prop="email">
        <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱"
            clearable
        ></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleLogin">登录</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, computed } from "vue";
import router from "@/router/index.js";
import { ElMessage } from 'element-plus';
import axios from 'axios';
import {useUserStore} from "@/store/user.js";
import {useRouter, useRoute} from "vue-router";
import { useTheme } from '@/composables/useTheme';
import { getUnreadTotal } from '@/api/chat'

const userStore = useUserStore();
const route = useRoute();
const { theme, toggleTheme } = useTheme();

// 顶栏根据路由高亮
const activeNav = computed(() => {
  const path = route.path
  if (path === '/' || path.startsWith('/video')) return '/'
  if (path.startsWith('/channel/animation')) return '/channel/animation'
  if (path.startsWith('/anime')) return '/anime'
  if (path.startsWith('/channel/movie')) return '/channel/movie'
  if (path.startsWith('/live')) return '/live'
  return ''
})

// 未读私信数（0 时不显示红点）
const unreadMsgCount = ref(0)
let unreadTimer = null

const refreshUnread = async () => {
  if (!localStorage.getItem('token') && !userStore.userInfo?.id) {
    unreadMsgCount.value = 0
    return
  }
  try {
    const res = await getUnreadTotal()
    if (res.code === 1) {
      unreadMsgCount.value = Number(res.data) || 0
    }
  } catch (e) {
    // 静默失败，避免刷屏
  }
}

const startUnreadPolling = () => {
  stopUnreadPolling()
  refreshUnread()
  unreadTimer = setInterval(refreshUnread, 30000)
}

const stopUnreadPolling = () => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
}

const handleNotificationClick = () => {
  ElMessage.info('通知功能开发中')
}

const goToMessage = () => {
  unreadMsgCount.value = 0
  router.push('/chat')
}

// 搜索框绑定值
const searchText = ref('')

const handleSearch = () => {
  const q = (searchText.value || '').trim()
  if (!q) return
  router.push({ path: '/search', query: { q } })
}

// 投稿按钮点击事件处理
function openTougao() {
  console.log('投稿');
  router.push('/contribute');
}

// f
function openShouYe(){
  console.log('首页');
  router.push('/');
}

function openLive(){
  router.push('/live');
}



// ============================================登陆========================
const dialogVisible = ref(false);
const loginFormRef = ref(null);
// 登录表单数据：字段为email（与模板输入框v-model一致）
const loginForm = reactive({
  email: '',
  password: ''
});

// 打开登录弹窗（保留原有逻辑，补充日志便于排查）
function openLoginDialog() {
  console.log('打开登录弹窗');
  dialogVisible.value = true;
}

// 表单验证规则
const loginRules = reactive({
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
});

// 处理登录逻辑

const handleLogin = async () => {
  try {
    console.log('进入登录逻辑，当前表单数据：', loginForm);

    // 执行表单验证（取消注释，确保输入合法）
    await loginFormRef.value.validate();
    console.log('表单验证通过，准备发送登录请求');

    // 发送登录请求
    const res = await axios.post('/api/pp/people/login', {
      email: loginForm.email,
      password: loginForm.password
    });
    console.log("后端返回的 res.data：", res.data); //

    // 关键：校验后端响应状态（code=1 为成功）
    if (res.data.code !== 1) {
      ElMessage.error(res.msg || "登录失败");
      return;
    }

    // 校验 token 是否存在
    if (!res.data?.data?.token) {
      ElMessage.error("登录失败：未获取到有效 token");
      return;
    }

    // 存储用户状态（Pinia + 本地存储）
    userStore.login(res.data.data);
    console.log("Pinia 存储成功，userInfo：", userStore.userInfo);
    console.log("isLogin：", userStore.isLogin); // 此时应为 true

    // 登录成功提示
    ElMessage.success('登录成功，正在刷新页面...');
    window.location.reload()
    // 重置表单
    loginForm.email = "";
    loginForm.password = "";
    dialogVisible.value = false;




  } catch (error) {
    console.error('登录失败详情：', error);
    if (error.name === 'Error') {
      ElMessage.warning(error.message || '表单验证失败');
    } else if (error.response) {
      ElMessage.error(`登录请求失败：${error.response.status}，请稍后重试`);
    } else {
      ElMessage.error('登录过程异常，请检查网络或联系客服');
    }
  }
};
// ============================================退出登陆=======================================
const handleLogout = () => {
  userStore.logout() // 调用 Pinia 的 logout 方法（清除 token 和 userInfo）
  ElMessage.success('退出登录成功')
  // 可选：退出后刷新页面或跳转首页（根据需求选择）
  // router.push('/') // 跳转首页
  window.location.reload() // 刷新页面（确保菜单状态同步）
}

// ===========================================注册相关=====================================
const registerDialogVisible = ref(false);
const registerFormRef = ref(null);

// 打开注册对话框的方法
const openRegisterDialog = () => {
  registerDialogVisible.value = true;
  // 打开时刷新验证码
  refreshCaptcha();
};

// 验证码图片
const captchaImg = ref('');
// 验证码ID（后端返回，用于校验）
const captchaId = ref('');
// 倒计时相关
const countDown = ref(0);
const isSending = ref(false);

// 注册表单数据
const registerForm = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  imageCaptcha: '',
  emailCaptcha: ''
});

// 表单验证规则
const registerRules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  imageCaptcha: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' }
  ],
  emailCaptcha: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' }
  ]
});

// 获取图形验证码
const refreshCaptcha = async () => {
  try {
    // 这里替换为你的实际API调用
    const res = await axios.get('/api/pp/people/captcha/image');
    console.log('请求发送成功res是:', res)
    captchaImg.value = res.data.data.imageBase64;
    console.log('相应的data是:',res.data);
    captchaId.value = res.data.data.captchaId;

  } catch (error) {
    ElMessage.error('获取验证码失败，请重试');
  }
};

// 发送邮箱验证码
const sendEmailCaptcha = async () => {
  // 先验证邮箱和图形验证码
  const emailValid = await validateField('email');
  const captchaValid = await validateField('imageCaptcha');

  // if (!emailValid || !captchaValid){
  //   console.log('校验失败-----');
  //   return;
  // }


  isSending.value = true;
  try {
    // 这里替换为你的实际API调用
    await axios.post('/api/pp/people/email', {
      email: registerForm.email,
      captchaId: captchaId.value,
      imageCaptcha: registerForm.imageCaptcha
    });



    ElMessage.success('验证码已发送，请查收邮件');
    // 开始倒计时
    countDown.value = 60;
    const timer = setInterval(() => {
      countDown.value--;
      if (countDown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);
    // 清空图形验证码，避免重复使用
    registerForm.imageCaptcha = '';
    // 刷新图形验证码
    await refreshCaptcha();
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '发送验证码失败');
  } finally {
    isSending.value = false;
  }
};


// 表单字段单独验证
const validateField = (field) => {
  return new Promise((resolve) => {
    if (!registerFormRef.value) {
      resolve(false);
      return;
    }
    registerFormRef.value.validateField(field, (error) => {
      resolve(!error);
    });
  });
};


// 提交注册
const handleRegister = async () => {
  // if (!registerFormRef.value) return;

  try {
    await registerFormRef.value.validate();
    // 调用注册接口
    const res = await axios.post('/api/pp/people/register', {
      email: registerForm.email,
      password: registerForm.password,
      emailCaptcha: registerForm.emailCaptcha
    });



    ElMessage.success('注册成功，请登录');
    registerDialogVisible.value = false;
    resetRegisterForm();
    // 可以在这里触发登录弹窗显示
    openLoginDialog();
  } catch (error) {
    if (error.name === 'Error') {
      ElMessage.error(error.message);
    } else {
      ElMessage.error('注册失败，请重试');
    }
  }
};

// 重置表单
const resetRegisterForm = () => {
  if (registerFormRef.value) {
    registerFormRef.value.resetFields();
  }
  refreshCaptcha();
};

// 初始化时获取图形验证码 + 未读轮询
onMounted(() => {
  refreshCaptcha();
  if (userStore.userInfo?.id || localStorage.getItem('token')) {
    startUnreadPolling()
  }
});

onUnmounted(() => {
  stopUnreadPolling()
});

// 登录后开始轮询未读
watch(
  () => userStore.userInfo?.id,
  (id) => {
    if (id) startUnreadPolling()
    else {
      stopUnreadPolling()
      unreadMsgCount.value = 0
    }
  }
);


//=========================== 设置 =============
function openSettingDialog(){
  router.push('/setting')
}

function openProfile(){
  const id = userStore.userInfo?.id
  if (!id) {
    ElMessage.warning('请先登录')
    dialogVisible.value = true
    return
  }
  router.push(`/user/${id}`)
}

</script>

<style scoped>
.header-container {
  background-color: var(--paper-white);
  border-bottom: 1px solid var(--line);
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  z-index: 1000;
  box-shadow: var(--shadow-sm);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 var(--space-md);
}

/* Logo样式 */
.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-right: var(--space-xl);
  cursor: pointer;
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.logo:hover {
  transform: translateY(-1px);
}

.logo:active {
  transform: scale(0.97);
}

.logo-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
  border-radius: var(--radius-sm);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.logo:hover .logo-img {
  transform: rotate(-8deg) scale(1.08);
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.35);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--brand);
  letter-spacing: -0.5px;
  background: linear-gradient(90deg, var(--brand), #00aeec);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  border-bottom: none;
  background: transparent !important;
}

.nav-menu .el-menu-item {
  font-size: 14px;
  height: 60px;
  line-height: 60px;
  margin: 0 var(--space-xs);
  border-bottom: 2px solid transparent;
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;
}

.nav-menu .el-menu-item::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--brand), #00aeec);
  transition: width var(--transition-base), left var(--transition-base);
  border-radius: 2px 2px 0 0;
}

.nav-menu .el-menu-item:hover {
  color: var(--brand);
  background-color: var(--brand-bg);
  transform: translateY(-1px);
}

.nav-menu .el-menu-item:hover::after {
  width: 60%;
  left: 20%;
}

.nav-menu .el-menu-item.is-active {
  color: var(--brand);
  border-bottom-color: transparent;
  font-weight: 600;
  background-color: var(--brand-bg);
}

.nav-menu .el-menu-item.is-active::after {
  width: 70%;
  left: 15%;
  box-shadow: 0 0 8px rgba(108, 92, 231, 0.55);
}

/* 右侧功能区 */
.right-actions {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-shrink: 0;
}

.search-input {
  width: 260px;
  transition: width var(--transition-base);
}

.search-input :deep(.el-input__wrapper) {
  background-color: var(--paper);
  border-radius: var(--radius-lg);
  padding: 0 var(--space-md);
  box-shadow: none;
  border: 1px solid var(--line);
  transition: all var(--transition-fast);
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px var(--brand-bg);
}

.search-input:focus-within {
  width: 320px;
}

.notification-badge {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast), transform var(--transition-fast);
}

.notification-badge:hover {
  background-color: var(--brand-bg);
  transform: translateY(-1px) scale(1.05);
}

.notification-badge:active {
  transform: scale(0.94);
}

.action-icon {
  color: var(--ink-secondary);
  transition: color var(--transition-fast);
}

.notification-badge:hover .action-icon {
  color: var(--brand);
}

/* 未读红点 */
.msg-badge {
  position: relative;
}

.dot-badge {
  position: absolute;
  top: 2px;
  right: 0;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: #f56c6c;
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
  box-sizing: border-box;
  pointer-events: none;
  font-weight: 600;
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: 8px 16px;
  font-weight: 500;
  border-radius: var(--radius-md);
  position: relative;
  overflow: hidden;
  transition: transform var(--transition-fast), box-shadow var(--transition-base);
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -80%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.35),
    transparent
  );
  transform: skewX(-20deg);
  transition: left 0.55s ease;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(108, 92, 231, 0.35);
}

.submit-btn:hover::before {
  left: 130%;
}

.submit-btn:active {
  transform: scale(0.97);
}

/* 用户菜单 */
.user-menu {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  white-space: nowrap;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast), transform var(--transition-fast);
}

.user-menu:hover {
  background-color: var(--brand-bg);
  transform: translateY(-1px);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border: 2px solid var(--line);
  transition: border-color var(--transition-fast), transform var(--transition-base), box-shadow var(--transition-base);
}

.user-menu:hover .user-avatar {
  border-color: var(--brand);
  transform: scale(1.06) rotate(3deg);
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.25);
}

.user-name {
  max-width: 80px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  color: var(--ink);
  font-weight: 500;
}

.arrow-icon {
  color: var(--mist-light);
  transition: transform var(--transition-fast);
}

.user-menu:hover .arrow-icon {
  color: var(--brand);
  transform: translateY(1px);
}

/* 验证码相关 */
.captcha-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.captcha-img {
  width: 100%;
  height: 40px;
  cursor: pointer;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
}

.refresh-btn {
  margin-top: var(--space-xs);
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 var(--space-sm);
  }

  .nav-menu {
    display: none;
  }

  .search-input {
    width: 180px;
  }

  .search-input:focus-within {
    width: 220px;
  }

  .submit-btn span {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
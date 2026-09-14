<template>
  <div class="settings-layout">
    <!-- 顶部导航栏 -->
    <el-container>
      <el-header class="settings-header">
        <div class="header-content">
          <el-button type="text" @click="goBack" class="back-btn">
            返回上一页
          </el-button>
          <h2 class="settings-title">账户设置</h2>
          <div class="header-right">
            <el-avatar :src="userStore.userInfo.avatar || defaultAvatar" size="36"></el-avatar>
            <span class="username ml-2">{{ userStore.userInfo.nickname || userStore.userInfo.username || '游客' }}</span>
          </div>
        </div>
      </el-header>

      <!-- 主体内容区：左侧导航 + 右侧设置面板 -->
      <el-container class="settings-body">
        <!-- 左侧导航栏 -->
        <el-aside width="220px" class="settings-aside">
          <el-menu
              default-active="profile"
              class="settings-menu"
              background-color="#f8f9fa"
              text-color="#333"
              active-text-color="#fb7299"
              @select="handleMenuChange"
          >
            <!-- 个人信息模块 -->
            <el-menu-item index="profile">
              <span slot="title">个人资料修改</span>
            </el-menu-item>

            <!-- 账号安全模块 -->
            <el-menu-item index="security">
              <span slot="title">账号安全</span>
            </el-menu-item>

            <!-- 播放设置模块（视频网站核心） -->
            <el-menu-item index="player">
              <span slot="title">播放设置</span>
            </el-menu-item>

            <!-- 画质与缓存模块 -->
            <el-menu-item index="quality">
              <span slot="title">画质与缓存</span>
            </el-menu-item>

            <!-- 通知设置模块 -->
            <el-menu-item index="notification">
              <span slot="title">通知设置</span>
            </el-menu-item>

            <!-- 隐私设置模块 -->
            <el-menu-item index="privacy">
              <span slot="title">隐私设置</span>
            </el-menu-item>

            <!-- 快捷键设置（视频网站实用功能） -->
            <el-menu-item index="shortcut">
              <span slot="title">播放快捷键</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 右侧设置内容区 -->
        <el-main class="settings-main">
          <!-- 1. 个人资料修改 -->
          <div v-if="activeTab === 'profile'" class="settings-panel">
            <h3 class="panel-title">个人资料修改</h3>
            <el-form :model="profileForm" label-width="100px" class="profile-form">
              <!-- 头像上传 -->
              <el-form-item label="用户头像">
                <el-upload
                    class="avatar-uploader"
                    action="/api/user/upload-avatar"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                    accept="image/jpeg,image/png"
                >
                  <el-avatar :src="profileForm.avatar || defaultAvatar" class="avatar-preview">
                    <i class="el-icon-plus avatar-uploader-icon"></i>
                  </el-avatar>
                </el-upload>
                <p class="form-hint">支持 JPG、PNG 格式，建议尺寸 200x200px</p>
              </el-form-item>

              <!-- 昵称修改 -->
              <el-form-item label="用户昵称" prop="nickname">
                <el-input
                    v-model="profileForm.nickname"
                    placeholder="请输入昵称"
                    maxlength="16"
                    show-word-limit
                ></el-input>
                <p class="form-hint">昵称长度不超过 16 个字符，可包含中英文、数字和下划线</p>
              </el-form-item>

              <!-- 个性签名 -->
              <el-form-item label="个性签名" prop="signature">
                <el-input
                    v-model="profileForm.signature"
                    placeholder="分享你的个性签名"
                    type="textarea"
                    :rows="3"
                    maxlength="80"
                    show-word-limit
                ></el-input>
                <p class="form-hint">签名长度不超过 80 个字符</p>
              </el-form-item>

              <!-- 联系信息 -->
              <el-form-item label="绑定邮箱">
                <el-input
                    v-model="profileForm.email"
                    disabled
                    placeholder="已绑定邮箱"
                ></el-input>
                <el-button type="text" @click="openEmailModify" class="modify-btn">修改邮箱</el-button>
              </el-form-item>

              <el-form-item label="绑定手机">
                <el-input
                    v-model="profileForm.phone"
                    placeholder="未绑定手机"
                    maxlength="11"
                ></el-input>
                <el-button type="text" @click="openPhoneBind" class="modify-btn">绑定手机</el-button>
              </el-form-item>

              <!-- 提交按钮 -->
              <el-form-item>
                <el-button type="primary" @click="saveProfile">保存修改</el-button>
                <el-button @click="resetProfileForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 2. 账号安全 -->
          <div v-if="activeTab === 'security'" class="settings-panel">
            <h3 class="panel-title">账号安全</h3>
            <div class="security-list">
              <!-- 密码修改 -->
              <div class="security-item">
                <div class="item-label">
                  <span class="item-name">密码修改</span>
                </div>
                <div class="item-desc">定期修改密码可提升账号安全性</div>
                <el-button type="text" @click="openPasswordModify">修改</el-button>
              </div>

              <!-- 登录设备管理 -->
              <div class="security-item">
                <div class="item-label">
                  <span class="item-name">登录设备管理</span>
                </div>
                <div class="item-desc">查看当前登录设备，下线异常设备</div>
                <el-button type="text" @click="openDeviceManage">管理</el-button>
              </div>

              <!-- 登录保护 -->
              <div class="security-item">
                <div class="item-label">
                  <span class="item-name">登录保护</span>
                </div>
                <div class="item-desc">开启后，新设备登录需验证邮箱/手机验证码</div>
                <el-switch
                    v-model="securityForm.loginProtect"
                    active-color="#fb7299"
                    @change="toggleLoginProtect"
                ></el-switch>
              </div>

              <!-- 第三方账号绑定 -->
              <div class="security-item">
                <div class="item-label">
                  <span class="item-name">第三方账号绑定</span>
                </div>
                <div class="item-desc">绑定微信、QQ 账号，快速登录</div>
                <div class="third-party-bind">
                  <el-button type="text" class="third-btn wechat-btn">
                    绑定微信
                  </el-button>
                  <el-button type="text" class="third-btn qq-btn">
                    绑定 QQ
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 3. 播放设置（视频网站核心） -->
          <div v-if="activeTab === 'player'" class="settings-panel">
            <h3 class="panel-title">播放设置</h3>
            <el-form :model="playerForm" label-width="120px" class="player-form">
              <!-- 默认播放画质 -->
              <el-form-item label="默认播放画质">
                <el-select
                    v-model="playerForm.defaultQuality"
                    placeholder="请选择默认画质"
                    class="form-select"
                >
                  <el-option label="自动（根据网络调整）" value="auto"></el-option>
                  <el-option label="流畅（480P）" value="480p"></el-option>
                  <el-option label="高清（720P）" value="720p"></el-option>
                  <el-option label="超清（1080P）" value="1080p"></el-option>
                  <el-option label="蓝光（2K）" value="2k"></el-option>
                  <el-option label="原画质（4K）" value="4k"></el-option>
                </el-select>
              </el-form-item>

              <!-- 自动播放设置 -->
              <el-form-item label="自动播放">
                <el-radio-group v-model="playerForm.autoPlay">
                  <el-radio label="开启">开启</el-radio>
                  <el-radio label="关闭">关闭</el-radio>
                  <el-radio label="仅WiFi下开启">仅WiFi下开启</el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 连续播放 -->
              <el-form-item label="连续播放">
                <el-switch
                    v-model="playerForm.continuePlay"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">开启后，当前视频播放完毕自动播放下一集</span>
              </el-form-item>

              <!-- 播放速度记忆 -->
              <el-form-item label="播放速度记忆">
                <el-switch
                    v-model="playerForm.speedMemory"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">下次打开视频时，自动恢复上次播放速度</span>
              </el-form-item>

              <!-- 音量记忆 -->
              <el-form-item label="音量记忆">
                <el-switch
                    v-model="playerForm.volumeMemory"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">下次打开视频时，自动恢复上次音量大小</span>
              </el-form-item>

              <!-- 弹幕设置 -->
              <el-form-item label="默认弹幕">
                <el-switch
                    v-model="playerForm.defaultDanmu"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">打开视频时默认显示弹幕</span>
              </el-form-item>

              <el-form-item label="弹幕透明度">
                <el-slider
                    v-model="playerForm.danmuOpacity"
                    :min="30"
                    :max="100"
                    :step="10"
                    show-input
                    input-size="small"
                ></el-slider>
                <span class="slider-desc">当前透明度：{{ playerForm.danmuOpacity }}%</span>
              </el-form-item>

              <!-- 提交按钮 -->
              <el-form-item>
                <el-button type="primary" @click="savePlayerSettings">保存设置</el-button>
                <el-button @click="resetPlayerSettings">恢复默认</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 4. 画质与缓存 -->
          <div v-if="activeTab === 'quality'" class="settings-panel">
            <h3 class="panel-title">画质与缓存</h3>
            <div class="quality-list">
              <!-- 缓存画质设置 -->
              <div class="quality-item">
                <div class="item-label">
                  <span class="item-name">默认缓存画质</span>
                </div>
                <el-select
                    v-model="qualityForm.cacheQuality"
                    placeholder="请选择缓存画质"
                    class="quality-select"
                >
                  <el-option label="流畅（480P）- 省空间" value="480p"></el-option>
                  <el-option label="高清（720P）- 平衡" value="720p"></el-option>
                  <el-option label="超清（1080P）- 高清" value="1080p"></el-option>
                </el-select>
              </div>

              <!-- 缓存路径 -->
              <div class="quality-item">
                <div class="item-label">
                  <span class="item-name">缓存存储路径</span>
                </div>
                <el-input
                    v-model="qualityForm.cachePath"
                    disabled
                    class="cache-path-input"
                ></el-input>
                <el-button type="text" @click="chooseCachePath" class="choose-path-btn">选择路径</el-button>
              </div>

              <!-- 缓存清理 -->
              <div class="quality-item">
                <div class="item-label">
                  <span class="item-name">缓存清理</span>
                </div>
                <div class="cache-size">已使用缓存：<span class="size-num">2.4GB</span></div>
                <el-button type="text" class="clear-cache-btn" @click="clearCache">
                  清理全部缓存
                </el-button>
              </div>

              <!-- 流量节省模式 -->
              <div class="quality-item">
                <div class="item-label">
                  <span class="item-name">流量节省模式</span>
                </div>
                <el-switch
                    v-model="qualityForm.trafficSaving"
                    active-color="#fb7299"
                    @change="toggleTrafficSaving"
                ></el-switch>
                <span class="switch-desc">移动网络下自动降低画质，减少流量消耗</span>
              </div>
            </div>
          </div>

          <!-- 5. 通知设置 -->
          <div v-if="activeTab === 'notification'" class="settings-panel">
            <h3 class="panel-title">通知设置</h3>
            <el-form :model="notificationForm" label-width="120px" class="notification-form">
              <!-- 系统通知 -->
              <el-form-item label="系统通知">
                <el-switch
                    v-model="notificationForm.systemNotice"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">接收账号安全、系统更新等通知</span>
              </el-form-item>

              <!-- 关注更新通知 -->
              <el-form-item label="关注更新">
                <el-switch
                    v-model="notificationForm.followUpdate"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">关注的UP主发布新视频时通知</span>
              </el-form-item>

              <!-- 评论回复通知 -->
              <el-form-item label="评论回复">
                <el-switch
                    v-model="notificationForm.commentReply"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">收到评论、回复、@ 时通知</span>
              </el-form-item>

              <!-- 点赞收藏通知 -->
              <el-form-item label="点赞收藏">
                <el-switch
                    v-model="notificationForm.likeCollect"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">视频、评论被点赞或收藏时通知</span>
              </el-form-item>

              <!-- 直播开播通知 -->
              <el-form-item label="直播开播">
                <el-switch
                    v-model="notificationForm.liveNotice"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">关注的UP主开播时通知</span>
              </el-form-item>

              <!-- 通知方式 -->
              <el-form-item label="通知方式">
                <el-checkbox-group v-model="notificationForm.noticeWay">
                  <el-checkbox label="站内信"></el-checkbox>
                  <el-checkbox label="邮件"></el-checkbox>
                  <el-checkbox label="手机短信"></el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <!-- 提交按钮 -->
              <el-form-item>
                <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 6. 隐私设置 -->
          <div v-if="activeTab === 'privacy'" class="settings-panel">
            <h3 class="panel-title">隐私设置</h3>
            <div class="privacy-list">
              <!-- 观看历史 -->
              <div class="privacy-item">
                <div class="item-label">
                  <span class="item-name">观看历史</span>
                </div>
                <el-select
                    v-model="privacyForm.viewHistory"
                    placeholder="请选择隐私权限"
                    class="privacy-select"
                >
                  <el-option label="仅自己可见" value="self"></el-option>
                  <el-option label="仅关注的人可见" value="follow"></el-option>
                  <el-option label="公开可见" value="public"></el-option>
                </el-select>
              </div>

              <!-- 收藏列表 -->
              <div class="privacy-item">
                <div class="item-label">
                  <span class="item-name">收藏列表</span>
                </div>
                <el-select
                    v-model="privacyForm.collection"
                    placeholder="请选择隐私权限"
                    class="privacy-select"
                >
                  <el-option label="仅自己可见" value="self"></el-option>
                  <el-option label="仅关注的人可见" value="follow"></el-option>
                  <el-option label="公开可见" value="public"></el-option>
                </el-select>
              </div>

              <!-- 动态发布 -->
              <div class="privacy-item">
                <div class="item-label">
                  <span class="item-name">动态发布</span>
                </div>
                <el-select
                    v-model="privacyForm.dynamic"
                    placeholder="请选择隐私权限"
                    class="privacy-select"
                >
                  <el-option label="仅自己可见" value="self"></el-option>
                  <el-option label="仅关注的人可见" value="follow"></el-option>
                  <el-option label="公开可见" value="public"></el-option>
                </el-select>
              </div>

              <!-- 个人资料可见性 -->
              <div class="privacy-item">
                <div class="item-label">
                  <span class="item-name">个人资料可见性</span>
                </div>
                <el-checkbox-group v-model="privacyForm.profileVisible">
                  <el-checkbox label="昵称"></el-checkbox>
                  <el-checkbox label="头像"></el-checkbox>
                  <el-checkbox label="个性签名"></el-checkbox>
                  <el-checkbox label="关注列表"></el-checkbox>
                  <el-checkbox label="粉丝列表"></el-checkbox>
                </el-checkbox-group>
              </div>

              <!-- 陌生人私信 -->
              <div class="privacy-item">
                <div class="item-label">
                  <span class="item-name">陌生人私信</span>
                </div>
                <el-switch
                    v-model="privacyForm.strangerMsg"
                    active-color="#fb7299"
                ></el-switch>
                <span class="switch-desc">允许未关注的用户发送私信</span>
              </div>

              <!-- 隐私政策 -->
              <div class="privacy-item">
                <div class="item-label">
                  <span class="item-name">隐私政策</span>
                </div>
                <el-button type="text" @click="openPrivacyPolicy">查看隐私政策</el-button>
              </div>
            </div>
          </div>

          <!-- 7. 播放快捷键 -->
          <div v-if="activeTab === 'shortcut'" class="settings-panel">
            <h3 class="panel-title">播放快捷键设置</h3>
            <p class="shortcut-hint">以下快捷键仅在视频播放页生效，点击输入框可自定义快捷键</p>
            <el-table :data="shortcutList" border class="shortcut-table">
              <el-table-column label="功能" width="200" align="center"></el-table-column>
              <el-table-column label="默认快捷键" width="200" align="center"></el-table-column>
              <el-table-column label="当前快捷键" align="center">
                <template #default="scope">
                  <el-input
                      v-model="scope.row.current"
                      placeholder="点击设置"
                      @focus="focusShortcutInput(scope.row)"
                      @blur="blurShortcutInput(scope.row)"
                      class="shortcut-input"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="scope">
                  <el-button type="text" @click="resetShortcut(scope.row)">恢复默认</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="shortcut-footer">
              <el-button type="primary" @click="saveShortcutSettings">保存快捷键</el-button>
              <el-button @click="resetAllShortcuts">恢复全部默认</el-button>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 模态框：修改密码 -->
    <el-dialog
        v-model="passwordDialogVisible"
        title="修改密码"
        width="400px"
        :close-on-click-modal="false"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPasswordModify">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/store/user";

// 路由和状态管理初始化
const router = useRouter();
const userStore = useUserStore();
const defaultAvatar = "https://picsum.photos/200/200"; // 默认头像

// 激活的标签页（与左侧菜单index对应）
const activeTab = ref("profile");

// 监听左侧菜单切换
const handleMenuChange = (index) => {
  activeTab.value = index;
};

// 回退上一页
const goBack = () => {
  router.back();
};

// ====================== 1. 个人资料表单 ======================
const profileForm = reactive({
  avatar: userStore.userInfo.avatar || "",
  nickname: userStore.userInfo.nickname || "",
  signature: userStore.userInfo.signature || "",
  email: userStore.userInfo.email || "",
  phone: userStore.userInfo.phone || ""
});

// 头像上传前校验
const beforeAvatarUpload = (rawFile) => {
  const isImage = rawFile.type === "image/jpeg" || rawFile.type === "image/png";
  const isLt2M = rawFile.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("请上传 JPG 或 PNG 格式的图片");
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB");
  }
  return isImage && isLt2M;
};

// 头像上传成功回调
const handleAvatarSuccess = (response, file) => {
  if (response.code === 1) {
    profileForm.avatar = response.data.avatarUrl;
    ElMessage.success("头像上传成功");
  } else {
    ElMessage.error("头像上传失败");
  }
};

// 保存个人资料
const saveProfile = async () => {
  try {
    // 模拟接口请求
    await new Promise((resolve) => setTimeout(resolve, 500));
    // 更新 Pinia 中的用户信息
    userStore.userInfo = {
      ...userStore.userInfo,
      ...profileForm
    };
    // 同步到本地存储
    localStorage.setItem("userInfo", JSON.stringify(userStore.userInfo));
    ElMessage.success("个人资料修改成功");
  } catch (error) {
    ElMessage.error("个人资料修改失败，请稍后重试");
    console.error(error);
  }
};

// 重置个人资料表单
const resetProfileForm = () => {
  profileForm.avatar = userStore.userInfo.avatar || "";
  profileForm.nickname = userStore.userInfo.nickname || "";
  profileForm.signature = userStore.userInfo.signature || "";
  profileForm.phone = userStore.userInfo.phone || "";
};

// 打开修改邮箱弹窗
const openEmailModify = () => {
  ElMessageBox.prompt("请输入新邮箱", "修改绑定邮箱", {
    confirmButtonText: "下一步",
    cancelButtonText: "取消",
    inputType: "email",
    inputPlaceholder: "请输入新邮箱地址"
  }).then(async ({ value }) => {
    // 模拟发送验证码
    ElMessage.success(`验证码已发送至 ${value}`);
    // 后续逻辑：输入验证码、确认修改...
  });
};

// 打开绑定手机弹窗
const openPhoneBind = () => {
  ElMessageBox.prompt("请输入手机号", "绑定手机", {
    confirmButtonText: "获取验证码",
    cancelButtonText: "取消",
    inputType: "tel",
    inputPlaceholder: "请输入11位手机号"
  }).then(async ({ value }) => {
    if (value.length !== 11) {
      ElMessage.error("请输入正确的手机号");
      return;
    }
    // 模拟发送验证码
    ElMessage.success(`验证码已发送至 ${value}`);
    // 后续逻辑：输入验证码、确认绑定...
  });
};

// ====================== 2. 账号安全表单 ======================
const securityForm = reactive({
  loginProtect: false // 登录保护默认关闭
});

// 切换登录保护
const toggleLoginProtect = (value) => {
  if (value) {
    ElMessage.success("登录保护已开启，新设备登录需验证");
  } else {
    ElMessage.warning("登录保护已关闭，账号安全性降低");
  }
};

// 打开修改密码弹窗
const passwordDialogVisible = ref(false);
const passwordFormRef = ref(null);
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

// 密码修改校验规则
const passwordRules = reactive({
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 8, message: "密码长度不能少于8位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
});

// 打开修改密码弹窗
const openPasswordModify = () => {
  passwordDialogVisible.value = true;
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
};

// 提交密码修改
const submitPasswordModify = async () => {
  try {
    await passwordFormRef.value.validate();
    // 模拟接口请求（验证原密码、修改新密码）
    await new Promise((resolve) => setTimeout(resolve, 500));
    ElMessage.success("密码修改成功，请重新登录");
    passwordDialogVisible.value = false;
    // 退出登录
    userStore.logout();
    router.push("/login");
  } catch (error) {
    console.error("密码修改失败：", error);
  }
};

// 打开登录设备管理
const openDeviceManage = () => {
  ElMessage.info("设备管理功能开发中...");
  // 后续可扩展：展示当前登录设备列表、下线操作等
};

// ====================== 3. 播放设置表单 ======================
const playerForm = reactive({
  defaultQuality: "auto", // 默认自动画质
  autoPlay: "仅WiFi下开启", // 默认仅WiFi自动播放
  continuePlay: true, // 默认开启连续播放
  speedMemory: true, // 默认开启播放速度记忆
  volumeMemory: true, // 默认开启音量记忆
  defaultDanmu: true, // 默认显示弹幕
  danmuOpacity: 70 // 弹幕默认透明度70%
});

// 保存播放设置
const savePlayerSettings = () => {
  ElMessage.success("播放设置保存成功");
  // 可扩展：将设置存储到 Pinia 或本地存储
};

// 恢复播放设置默认值
const resetPlayerSettings = () => {
  playerForm.defaultQuality = "auto";
  playerForm.autoPlay = "仅WiFi下开启";
  playerForm.continuePlay = true;
  playerForm.speedMemory = true;
  playerForm.volumeMemory = true;
  playerForm.defaultDanmu = true;
  playerForm.danmuOpacity = 70;
  ElMessage.success("播放设置已恢复默认");
};

// ====================== 4. 画质与缓存表单 ======================
const qualityForm = reactive({
  cacheQuality: "720p", // 默认缓存高清
  cachePath: "C:/Users/xxx/Documents/biliplus/cache", // 默认缓存路径
  trafficSaving: true // 默认开启流量节省模式
});

// 切换流量节省模式
const toggleTrafficSaving = (value) => {
  if (value) {
    ElMessage.success("流量节省模式已开启");
  } else {
    ElMessage.warning("流量节省模式已关闭");
  }
};

// 选择缓存路径
const chooseCachePath = () => {
  ElMessage.info("缓存路径选择功能开发中...");
  // 后续可集成文件选择器组件
};

// 清理缓存
const clearCache = () => {
  ElMessageBox.confirm("确定要清理全部缓存吗？清理后不可恢复", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    // 模拟清理缓存
    setTimeout(() => {
      ElMessage.success("缓存清理成功");
    }, 500);
  });
};

// ====================== 5. 通知设置表单 ======================
const notificationForm = reactive({
  systemNotice: true, // 系统通知默认开启
  followUpdate: true, // 关注更新默认开启
  commentReply: true, // 评论回复默认开启
  likeCollect: true, // 点赞收藏默认开启
  liveNotice: false, // 直播开播默认关闭
  noticeWay: ["站内信", "邮件"] // 默认通知方式
});

// 保存通知设置
const saveNotificationSettings = () => {
  ElMessage.success("通知设置保存成功");
};

// ====================== 6. 隐私设置表单 ======================
const privacyForm = reactive({
  viewHistory: "self", // 观看历史仅自己可见
  collection: "self", // 收藏列表仅自己可见
  dynamic: "follow", // 动态仅关注的人可见
  profileVisible: ["昵称", "头像", "个性签名"], // 个人资料可见项
  strangerMsg: false // 陌生人私信默认关闭
});

// 打开隐私政策
const openPrivacyPolicy = () => {
  ElMessage.info("隐私政策页面开发中...");
  // 后续可跳转至隐私政策页面
};

// ====================== 7. 快捷键设置 ======================
const shortcutList = reactive([
  {
    功能: "播放/暂停",
    默认: "空格",
    current: "空格",
    key: "playPause"
  },
  {
    功能: "快进",
    默认: "→",
    current: "→",
    key: "fastForward"
  },
  {
    功能: "快退",
    默认: "←",
    current: "←",
    key: "fastBackward"
  },
  {
    功能: "音量增加",
    默认: "↑",
    current: "↑",
    key: "volumeUp"
  },
  {
    功能: "音量减少",
    默认: "↓",
    current: "↓",
    key: "volumeDown"
  },
  {
    功能: "全屏/退出全屏",
    默认: "F",
    current: "F",
    key: "fullScreen"
  },
  {
    功能: "显示/隐藏弹幕",
    默认: "D",
    current: "D",
    key: "toggleDanmu"
  },
  {
    功能: "倍速播放",
    默认: "S",
    current: "S",
    key: "speed"
  },
  {
    功能: "下一集",
    默认: "N",
    current: "N",
    key: "nextEpisode"
  },
  {
    功能: "上一集",
    默认: "P",
    current: "P",
    key: "prevEpisode"
  }
]);

// 快捷键输入框聚焦
const focusShortcutInput = (row) => {
  ElMessage.info("按下键盘按键设置快捷键（支持单个字母、数字、方向键、空格）");
};

// 快捷键输入框失焦
const blurShortcutInput = (row) => {
  // 这里可添加快捷键校验逻辑（如禁止重复、禁止特殊字符等）
};

// 恢复单个快捷键默认值
const resetShortcut = (row) => {
  row.current = row.默认;
};

// 恢复全部快捷键默认值
const resetAllShortcuts = () => {
  shortcutList.forEach((item) => {
    item.current = item.默认;
  });
  ElMessage.success("所有快捷键已恢复默认");
};

// 保存快捷键设置
const saveShortcutSettings = () => {
  ElMessage.success("快捷键设置保存成功");
  // 可扩展：将快捷键设置存储到本地存储
};

// 初始化：监听左侧菜单切换（Element Plus 菜单默认事件）
onMounted(() => {
  // 初始化时同步用户信息到表单
  profileForm.avatar = userStore.userInfo.avatar || "";
  profileForm.nickname = userStore.userInfo.nickname || "";
  profileForm.signature = userStore.userInfo.signature || "";
  profileForm.email = userStore.userInfo.email || "";
  profileForm.phone = userStore.userInfo.phone || "";
});
</script>

<style scoped>
/* 整体布局样式 */
.settings-layout {
  height: 100vh;
  overflow: hidden;
}

/* 顶部导航栏 */
.settings-header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
  height: 60px !important;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.back-btn {
  color: #333;
}

.settings-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.username {
  font-size: 14px;
  color: #333;
  margin-left: 8px;
}

/* 主体内容区 */
.settings-body {
  height: calc(100vh - 60px) !important;
  overflow: hidden;
}

/* 左侧导航栏 */
.settings-aside {
  background-color: #f8f9fa;
  border-right: 1px solid #eee;
}

.settings-menu {
  height: 100%;
  border-right: none;
}

/* 右侧设置内容区 */
.settings-main {
  padding: 24px;
  background-color: #fff;
  overflow-y: auto;
  height: 100%;
}

/* 设置面板通用样式 */
.settings-panel {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
  padding: 24px;
  margin-bottom: 24px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

/* 个人资料表单样式 */
.profile-form {
  max-width: 800px;
}

.avatar-uploader {
  display: flex;
  align-items: center;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  cursor: pointer;
  border: 1px dashed #dcdfe6;
}

.avatar-uploader-icon {
  font-size: 24px;
  color: #8c939d;
}

.form-hint {
  font-size: 12px;
  color: #8c939d;
  margin-top: 8px;
  margin-bottom: 0;
}

.modify-btn {
  color: #fb7299;
  padding: 0;
  margin-left: 12px;
}

/* 账号安全样式 */
.security-list {
  max-width: 800px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.security-item:last-child {
  border-bottom: none;
}

.item-label {
  display: flex;
  align-items: center;
}

.item-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.item-desc {
  font-size: 12px;
  color: #8c939d;
  margin-left: 30px;
  flex: 1;
}

.third-party-bind {
  display: flex;
  gap: 20px;
}

.third-btn {
  display: flex;
  align-items: center;
  color: #333;
}

.wechat-btn {
  color: #4cd964;
}

.qq-btn {
  color: #1da1f2;
}

/* 播放设置样式 */
.player-form {
  max-width: 800px;
}

.form-select {
  width: 300px;
}

.switch-desc {
  font-size: 12px;
  color: #8c939d;
  margin-left: 12px;
}

.slider-desc {
  font-size: 12px;
  color: #8c939d;
  margin-left: 12px;
}

/* 画质与缓存样式 */
.quality-list {
  max-width: 800px;
}

.quality-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.quality-item:last-child {
  border-bottom: none;
}

.quality-select {
  width: 200px;
}

.cache-path-input {
  width: 400px;
  background-color: #f5f5f5;
}

.choose-path-btn {
  color: #fb7299;
  padding: 0;
  margin-left: 12px;
}

.cache-size {
  font-size: 12px;
  color: #8c939d;
  margin-left: 30px;
  flex: 1;
}

.size-num {
  color: #fb7299;
  font-weight: 500;
}

.clear-cache-btn {
  color: #ff4d4f;
  display: flex;
  align-items: center;
}

/* 通知设置样式 */
.notification-form {
  max-width: 800px;
}

/* 隐私设置样式 */
.privacy-list {
  max-width: 800px;
}

.privacy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.privacy-item:last-child {
  border-bottom: none;
}

.privacy-select {
  width: 200px;
}

/* 快捷键设置样式 */
.shortcut-hint {
  font-size: 12px;
  color: #8c939d;
  margin-bottom: 16px;
}

.shortcut-table {
  max-width: 800px;
  margin-bottom: 20px;
}

.shortcut-input {
  width: 120px;
  text-align: center;
}

.shortcut-footer {
  display: flex;
  gap: 12px;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .settings-main {
    padding: 16px;
  }

  .settings-panel {
    padding: 16px;
  }

  .profile-form,
  .security-list,
  .player-form,
  .quality-list,
  .notification-form,
  .privacy-list {
    max-width: 100%;
  }

  .cache-path-input {
    width: 300px;
  }
}

@media (max-width: 768px) {
  .settings-aside {
    width: 180px !important;
  }

  .form-select {
    width: 200px;
  }

  .cache-path-input {
    width: 200px;
  }

  .shortcut-table {
    font-size: 12px;
  }

  .shortcut-input {
    width: 100px;
  }
}
</style>
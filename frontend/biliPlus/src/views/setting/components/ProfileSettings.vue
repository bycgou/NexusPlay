<script setup lang="ts">
import {ref,reactive,onMounted} from "vue";
import {useRoute} from "vue-router";
import {ElMessage,ElMessageBox} from "element-plus";
import {useUserStore} from "@/store/user.ts";
import {uploadImage} from "@/utils/ImageUploader.ts";
import {updateUserInfo} from "@/api/user.ts";
import {Plus} from '@element-plus/icons-vue';
// 路由管理
const route = useRoute();
const userStore = useUserStore();
const defaultAvatar = "https://picsum.photos/200/200"; // 默认头像

const profileForm = reactive({
  avatar: userStore.userInfo.avatar || "",
  nickname: userStore.userInfo.nickname || "",
  signature: userStore.userInfo.signature || "",
  email: userStore.userInfo.email || "",
  phone: userStore.userInfo.phone || "",
});

// 头像上传前校验
const beforeAvatarUpload=(rawFile)=>{
  const isImage = rawFile.type === "image/jpeg" || rawFile.type === "image/png";
  const isLt2M = rawFile.size / 1024 / 1024 < 2;
  if(!isImage){
    ElMessage.error("请上传 JPG 或 PNG 格式的图片");
  }
  if(!isLt2M){
    ElMessage.error("图片大小不能超过 2MB");
  }
  return isImage && isLt2M;
}

// 上传头像
const handleAvatarUpload = async(uploadFile: {raw:File})=>{
  const file = uploadFile.raw;
  if(!file) return;
  try {
    const avatarUrl = await uploadImage(file,{
      uploadUrl:'/api/pp/upload/cover',
      maxSize: 2 * 1024 * 1024,
      showNotification: true,
    });
    console.log('后端响应的图片地址:',avatarUrl)
    // 更新表单和pinia
    profileForm.avatar = avatarUrl;
    userStore.userInfo.avatar = avatarUrl;
    console.log('更新pinia中的用户信息:',userStore.userInfo)

  }catch ( error){
    console.error(error)
  }
};

// 保存个人资料
const saveProfile = async () => {
  try {
    console.log('提交个人资料开始了----:',profileForm)
    // 发送接口请求
    const res = await updateUserInfo({
          username: userStore.userInfo.username,
          nickname: profileForm.nickname,
          signature: profileForm.signature,
          avatar: profileForm.avatar,
    });
    console.log('后端响应的 res.data:',res.data)
    if(res.code === 200|| res.code===1){
      ElMessage.success("个人资料修改成功");
      // 更新pinia中的用户信息
      userStore.userInfo.nickname = profileForm.nickname;
      userStore.userInfo.signature = profileForm.signature;
      userStore.userInfo.avatar = profileForm.avatar;
      // 同步到本地存储
      localStorage.setItem("userInfo",JSON.stringify(userStore.userInfo));
    }else{
      ElMessage.error("个人资料修改失败,请重试");
      throw new Error(res.msg)
    }

  }catch (error){
    console.error('个人资料修改失败',error);
    ElMessage.error("个人资料修改失败,请重试")
  }
};

// 打开修改邮箱弹窗
const openEmailModify = () => {
  ElMessageBox.prompt("请输入新邮箱",{
    confirmButtonText: "下一步",
    cancelButtonText: "取消",
    inputType: "email",

  }).then(
      async ({ value }) => {
        // 模拟发送验证码
        ElMessage.success(`验证码已发送至 ${value}`);
        // 后续逻辑：输入验证码、确认修改...
      }
  );
};

// 打开绑定手机弹窗
const openPhoneModify = () => {
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
}

// 重置
const resetProfileForm = () => {
  Object.assign(profileForm, {
    avatar: userStore.userInfo.avatar || "",
    nickname: userStore.userInfo.nickname || "",
    signature: userStore.userInfo.signature || "",
    email: userStore.userInfo.email || "",
    phone: userStore.userInfo.phone || "",
  });
}
</script>

<template>
  <div class="settings-panel">
    <h3 class="panel-title">个人资料修改</h3>
    <p class="panel-desc">更新头像、昵称与个性签名</p>
    <el-form :model="profileForm" label-width="100px" class="profile-form">
<!--      头像上传-->
      <el-form-item>
        <el-upload
            class="avatar-uploader"
            :auto-upload="false"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            accept="image/jpeg,image/png"
            @change="handleAvatarUpload"
            >
          <el-avatar :size="80" :src="profileForm.avatar || defaultAvatar" class="avatar-preview">
            <div v-if="!profileForm.avatar" class="avatar-add-mask">
              <el-icon><Plus /></el-icon>
            </div>
          </el-avatar>
        </el-upload>
        <p class="form-hint">支持 JPG PNG 格式,建议尺寸 200x200 px</p>
      </el-form-item>
<!--      昵称修改-->
      <el-form-item label="用户昵称" prop="nickname">
        <el-input
          v-model="profileForm.nickname"
          placeholder="请输入昵称"
          maxlength="16"
          show-word-limit
        >
        </el-input>
        <p class="form-hint"> 昵称长度不能超过 16 个字符</p>
      </el-form-item>

<!--      个性签名-->
      <el-form-item label="个性签名" prop="signature">
        <el-input
            v-model="profileForm.signature"
            placeholder="分享你的个性签名"
            type="textarea"
            :rows="3"
            maxlength="80"
            show-word-limit
        >
        </el-input>
        <p class="form-hint"> 个性签名长度不能超过 80 个字符</p>
      </el-form-item >
<!--      联系信息-->
      <el-form-item label="已绑定邮箱">
        <el-input placeholder="已绑定邮箱"  disabled v-model="profileForm.email"></el-input>
        <el-button type="text" @click="openEmailModify"> 修改邮箱</el-button>
      </el-form-item>

      <el-form-item label="绑定手机">
        <el-input placeholder="未绑定手机"  disabled v-model="profileForm.phone"></el-input>
        <el-button type="text" @click="openPhoneModify"> 绑定手机</el-button>
      </el-form-item>

<!--      提交按钮-->
      <el-form-item>
        <el-button type="primary" @click="saveProfile">保存修改</el-button>
        <el-button @click="resetProfileForm">重置</el-button>
      </el-form-item>



    </el-form>

  </div>

</template>

<style scoped>
@import './settings-common.css';

.avatar-preview {
  cursor: pointer;
  transition: opacity var(--transition-fast);
}

.avatar-preview:hover {
  opacity: 0.9;
}
</style>
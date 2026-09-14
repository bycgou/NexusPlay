<script setup lang="ts">
import {UserFilled,Film} from '@element-plus/icons-vue'
import {ref,computed} from "vue";
import {useRouter} from "vue-router";
import {useUserStore} from "@/store/user.ts";

const router = useRouter();
const userStore = useUserStore();
const defaultAvatar = ref('https://picsum.photos/200/200');
const activeTab = ref('profile')


const goBack = ()=>{
  router.back();
}
const handleMenuChange = (index) => {
  activeTab.value = index;
  router.push({
    name: index,
  });
};


</script>

<template>
  <div class="common-layout">
    <el-container>
<!--      顶部导航栏-->
      <el-header>
        <div class="header-content">
<!--          左侧-->
          <el-button type="text" @click="goBack">返回上一页</el-button>

<!--          中间-->
          <h2 style="margin: 0; font-size: 1.2rem;">账户设置</h2>
<!--          右侧-->

          <div class="header-right">
            <el-avatar :src="userStore.userInfo.avatar || defaultAvatar" />
            <span>{{ userStore.userInfo.nickname||'游客'}}</span>
          </div>

        </div>

      </el-header>

      <el-container>
        <el-aside class="aside-content" width="200px">
         <el-menu
             :default-active="activeTab"
             @select="handleMenuChange"
             active-text-color="#fb7299"
             background-color="#f8f9fa"
             text-color="#333"
         >
           <el-menu-item index="profile">
             <el-icon><UserFilled /></el-icon>
             <span>个人资料设置</span>
           </el-menu-item>
           <el-menu-item index="videoSet">
             <el-icon><Film /></el-icon>
             <span>稿件设置</span>
           </el-menu-item>

           <el-menu-item index="security">
             <span>账号安全</span>
           </el-menu-item>

           <el-menu-item index="player">
             <span>播放设置</span>
           </el-menu-item>

           <el-menu-item index="quality">
             <span>画质与缓存</span>
           </el-menu-item>

           <el-menu-item index="notification">
             <span>通知设置</span>
           </el-menu-item>

           <el-menu-item index="privacy">
             <span>隐私设置</span>
           </el-menu-item>

           <el-menu-item index="shortcut">
             <span>快捷键设置</span>
           </el-menu-item>

         </el-menu>

        </el-aside>
        <el-main class="main-content">
<!--          <component :is="router.currentRoute.value.meta.component" />-->
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>


<style scoped>
.header-content{
  display: flex;
  justify-content: space-between; /* 左右对齐 */
  align-items: center;             /* 垂直居中 */
  padding: 0 20px;
  height: 60px; /* 调整高度，避免过长 */

}
.aside-content{

}
.main-content{

}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right span {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
}

</style>
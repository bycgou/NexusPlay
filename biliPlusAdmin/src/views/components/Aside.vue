<template>
  <div class="aside-root">
    <div class="brand">
      <span class="brand-logo">B</span>
      <div class="brand-text">
        <div class="brand-title">BiliPlus Admin</div>
        <div class="brand-sub">内容管理后台</div>
      </div>
    </div>
    <el-menu
        :default-active="activeMenu"
        active-text-color="#ffffff"
        background-color="transparent"
        class="side-menu"
        text-color="rgba(255,255,255,0.72)"
        router
    >
      <el-menu-item index="/Home/VideoShenHe" class="menu-item">
        <el-icon><VideoPlay /></el-icon>
        <span>视频审核</span>
      </el-menu-item>
      <el-menu-item index="/Home/Banner" class="menu-item">
        <el-icon><Picture /></el-icon>
        <span>轮播图管理</span>
      </el-menu-item>
      <el-menu-item index="/Home/Category" class="menu-item">
        <el-icon><Grid /></el-icon>
        <span>分类管理</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { VideoPlay, Grid, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/Home/VideoShenHe')) return '/Home/VideoShenHe'
  if (path.startsWith('/Home/Banner')) return '/Home/Banner'
  if (path.startsWith('/Home/Category')) return '/Home/Category'
  return path
})
</script>

<style scoped>
.aside-root {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 16px 12px;
}
.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fb7299;
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.brand-title {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.2;
}
.brand-sub {
  color: rgba(255, 255, 255, 0.45);
  font-size: 11px;
}
.side-menu {
  border-right: none;
  background: transparent;
  flex: 1;
}
.menu-item {
  margin: 4px 10px;
  border-radius: 8px;
}
.menu-item.is-active {
  background: #fb7299 !important;
  color: #fff !important;
}
.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
}
</style>

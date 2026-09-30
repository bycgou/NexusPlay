<template>
  <div class="aside-root">
    <div class="brand">
      <div class="brand-logo">N</div>
      <div class="brand-text">
        <div class="brand-title">NexusPlay</div>
        <div class="brand-sub">管理后台</div>
      </div>
    </div>
    <el-scrollbar class="menu-scroll">
      <el-menu
          :default-active="activeMenu"
          :default-openeds="defaultOpeneds"
          :unique-opened="false"
          active-text-color="#ffffff"
          background-color="transparent"
          class="side-menu"
          text-color="#475569"
          router
      >
        <el-menu-item index="/Home/Dashboard" class="menu-item">
          <el-icon><Odometer /></el-icon>
          <span>数据看板</span>
        </el-menu-item>

        <el-sub-menu index="governance">
          <template #title>
            <el-icon><Warning /></el-icon>
            <span>内容治理</span>
          </template>
          <el-menu-item index="/Home/VideoShenHe" class="menu-item">视频审核</el-menu-item>
          <el-menu-item index="/Home/Report" class="menu-item">举报处理</el-menu-item>
          <el-menu-item index="/Home/SensitiveWord" class="menu-item">敏感词治理</el-menu-item>
          <el-menu-item index="/Home/Governance" class="menu-item">治理报表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="user-ops">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户与运营</span>
          </template>
          <el-menu-item index="/Home/User" class="menu-item">用户管理</el-menu-item>
          <el-menu-item index="/Home/Notification" class="menu-item">系统通知</el-menu-item>
          <el-menu-item index="/Home/OperationLog" class="menu-item">操作日志</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="content-ops">
          <template #title>
            <el-icon><Grid /></el-icon>
            <span>内容运营</span>
          </template>
          <el-menu-item index="/Home/Banner" class="menu-item">轮播图</el-menu-item>
          <el-menu-item index="/Home/Category" class="menu-item">分类管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="live-gift">
          <template #title>
            <el-icon><VideoCamera /></el-icon>
            <span>直播与礼物</span>
          </template>
          <el-menu-item index="/Home/Live" class="menu-item">直播管理</el-menu-item>
          <el-menu-item index="/Home/LiveCategory" class="menu-item">直播分区</el-menu-item>
          <el-menu-item index="/Home/Gift" class="menu-item">礼物管理</el-menu-item>
          <el-menu-item index="/Home/GiftRecord" class="menu-item">打赏流水</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="wallet">
          <template #title>
            <el-icon><Wallet /></el-icon>
            <span>资金</span>
          </template>
          <el-menu-item index="/Home/WalletTx" class="menu-item">钱包账变</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Grid,
  VideoCamera,
  Warning,
  Wallet,
  User,
  Odometer
} from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/Home/VideoShenHe')) return '/Home/VideoShenHe'
  if (path.startsWith('/Home/Report')) return '/Home/Report'
  if (path.startsWith('/Home/SensitiveWord')) return '/Home/SensitiveWord'
  if (path.startsWith('/Home/User')) return '/Home/User'
  if (path.startsWith('/Home/Governance')) return '/Home/Governance'
  if (path.startsWith('/Home/Banner')) return '/Home/Banner'
  if (path.startsWith('/Home/Category')) return '/Home/Category'
  if (path.startsWith('/Home/LiveCategory')) return '/Home/LiveCategory'
  if (path.startsWith('/Home/Live')) return '/Home/Live'
  if (path.startsWith('/Home/GiftRecord')) return '/Home/GiftRecord'
  if (path.startsWith('/Home/WalletTx')) return '/Home/WalletTx'
  if (path.startsWith('/Home/Notification')) return '/Home/Notification'
  if (path.startsWith('/Home/Gift')) return '/Home/Gift'
  return path
})

/** 自动展开当前路由所属分组 */
const defaultOpeneds = computed(() => {
  const p = route.path
  const groups: string[] = []
  if (
    p.startsWith('/Home/VideoShenHe') ||
    p.startsWith('/Home/Report') ||
    p.startsWith('/Home/SensitiveWord') ||
    p.startsWith('/Home/Governance')
  ) {
    groups.push('governance')
  }
  if (
    p.startsWith('/Home/User') ||
    p.startsWith('/Home/Notification') ||
    p.startsWith('/Home/OperationLog')
  ) {
    groups.push('user-ops')
  }
  if (p.startsWith('/Home/Banner') || p.startsWith('/Home/Category')) {
    groups.push('content-ops')
  }
  if (
    p.startsWith('/Home/Live') ||
    p.startsWith('/Home/Gift')
  ) {
    groups.push('live-gift')
  }
  if (p.startsWith('/Home/WalletTx')) {
    groups.push('wallet')
  }
  return groups
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
  padding: 18px 16px 14px;
  flex-shrink: 0;
}
.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563EB 0%, #60A5FA 100%);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
}
.brand-title {
  color: #1e293b;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.2;
}
.brand-sub {
  color: #94a3b8;
  font-size: 11px;
}
.menu-scroll {
  flex: 1;
  min-height: 0;
}
.side-menu {
  border-right: none;
  background: transparent;
}
.side-menu :deep(.el-sub-menu__title),
.menu-item {
  margin: 2px 10px;
  border-radius: 8px;
  height: 42px;
  line-height: 42px;
}
.side-menu :deep(.el-sub-menu__title:hover),
.menu-item:hover {
  background: #eff6ff;
}
.side-menu :deep(.el-sub-menu .el-menu) {
  background: #f8fafc;
  border-radius: 8px;
  margin: 0 10px 6px;
}
.side-menu :deep(.el-sub-menu .menu-item) {
  margin: 2px 6px;
  height: 38px;
  line-height: 38px;
  padding-left: 36px !important;
}
.side-menu :deep(.el-menu-item.is-active),
.menu-item.is-active {
  background: #2563EB !important;
  color: #fff !important;
}
.side-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #2563EB !important;
}
</style>

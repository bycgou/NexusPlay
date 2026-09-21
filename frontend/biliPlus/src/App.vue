<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/views/home/Header.vue'
import MobileTabBar from '@/components/MobileTabBar.vue'
import { useTheme } from '@/composables/useTheme'

useTheme()

const route = useRoute()

// 登录页不显示顶栏与底部导航
// 注册走 Header 内的弹窗（/?register=1），因此注册场景必须显示顶栏
const hideChromePaths = ['/login']
const showChrome = computed(() => !hideChromePaths.includes(route.path))
</script>

<template>
  <div class="app-root" :class="{ 'has-fixed-header': showChrome }">
    <div v-if="showChrome" class="global-header">
      <Header />
    </div>
    <div class="app-body">
      <router-view></router-view>
    </div>
    <MobileTabBar v-if="showChrome" />
  </div>
</template>

<style>
html,
body,
#app {
  margin: 0;
  padding: 0;
  min-height: 100%;
}

.app-root {
  min-height: 100vh;
}

/* 全局吸顶导航 */
.global-header {
  position: fixed !important;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  width: 100%;
  box-shadow: 0 1px 8px rgba(15, 23, 42, 0.08);
  background-color: var(--paper-white, #fff) !important;
}

/* 有固定顶栏时给内容留出高度 */
.has-fixed-header .app-body {
  padding-top: 60px;
}

@media (max-width: 768px) {
  .has-fixed-header .app-body {
    padding-bottom: 56px;
  }
}
</style>

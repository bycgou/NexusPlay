<template>
  <div class="common-layout">
    <el-container class="layout-root">
      <el-aside width="220px" class="side-wrap">
        <Aside />
      </el-aside>
      <el-container class="main-wrap">
        <el-header class="top-header" height="56px">
          <Header />
        </el-header>
        <el-main class="content-main">
          <Main />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import Aside from '@/views/components/Aside.vue'
import Header from '@/views/components/Header.vue'
import Main from '@/views/components/Main.vue'
</script>

<style scoped>
.common-layout,
.layout-root {
  height: 100vh;
  overflow: hidden;
}
.side-wrap {
  background: linear-gradient(180deg, #1f2d3d 0%, #2b3a4a 100%);
  overflow: auto;
}
.top-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.content-main {
  background: #f5f7fa;
  padding: 0;
  overflow: auto;
}
</style>

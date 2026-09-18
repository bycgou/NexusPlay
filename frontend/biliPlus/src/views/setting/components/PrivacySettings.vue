<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useLocalSettings } from '@/composables/useLocalSettings'

const { settings, reset } = useLocalSettings('privacy', {
  showFollowing: true,
  showLiked: false,
  showFavorites: false,
  allowFollow: true,
  allowMessage: true,
  allowSearch: true,
  showOnline: true,
  showHistory: false
})

const handleReset = () => {
  reset()
}
</script>

<template>
  <div class="settings-panel">
    <h3 class="panel-title">隐私设置</h3>
    <p class="panel-desc">控制他人能看到哪些个人信息与互动记录</p>

    <h4 class="section-title">个人主页展示</h4>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">公开关注列表</p>
          <p class="desc">其他用户可在你的主页查看你关注了谁</p>
        </div>
        <el-switch v-model="settings.showFollowing" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">公开点赞视频</p>
          <p class="desc">允许他人查看你点赞过的视频</p>
        </div>
        <el-switch v-model="settings.showLiked" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">公开收藏夹</p>
          <p class="desc">允许他人查看你收藏的视频</p>
        </div>
        <el-switch v-model="settings.showFavorites" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">公开浏览历史</p>
          <p class="desc">在主页展示最近观看记录</p>
        </div>
        <el-switch v-model="settings.showHistory" />
      </div>
    </div>

    <h4 class="section-title" style="margin: 28px 0 8px">互动权限</h4>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">允许他人关注</p>
          <p class="desc">关闭后他人无法关注你的账号</p>
        </div>
        <el-switch v-model="settings.allowFollow" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">允许私信</p>
          <p class="desc">是否接收其他用户发送的私信</p>
        </div>
        <el-switch v-model="settings.allowMessage" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">允许被搜索</p>
          <p class="desc">关闭后将无法通过昵称搜索到你</p>
        </div>
        <el-switch v-model="settings.allowSearch" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">显示在线状态</p>
          <p class="desc">私信列表中展示是否在线</p>
        </div>
        <el-switch v-model="settings.showOnline" />
      </div>
    </div>

    <div class="footer-actions">
      <el-button @click="handleReset">恢复默认</el-button>
    </div>
  </div>
</template>

<style scoped>
@import './settings-common.css';
</style>

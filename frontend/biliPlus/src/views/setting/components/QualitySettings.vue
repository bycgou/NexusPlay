<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useLocalSettings } from '@/composables/useLocalSettings'

const { settings, reset } = useLocalSettings('quality', {
  preferredQuality: 'auto' as 'auto' | '1080p' | '720p' | '480p' | '360p',
  hardwareDecode: true,
  danmakuMask: false,
  autoQuality: true,
  maxCacheMB: 500,
  preloadNext: true
})

const cacheUsedMB = ref(128)
const clearing = ref(false)

const handleReset = () => {
  reset()
}

const handleClearCache = () => {
  ElMessageBox.confirm('将清除本地缓存的播放记录与预加载资源，确认继续？', '清除缓存', {
    confirmButtonText: '清除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      clearing.value = true
      setTimeout(() => {
        cacheUsedMB.value = 0
        clearing.value = false
        ElMessage.success('缓存已清除')
      }, 600)
    })
    .catch(() => {})
}
</script>

<template>
  <div class="settings-panel">
    <h3 class="panel-title">画质与缓存</h3>
    <p class="panel-desc">设置默认画质、解码方式与本地缓存</p>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">默认画质</p>
          <p class="desc">播放时优先选择的清晰度</p>
        </div>
        <el-select v-model="settings.preferredQuality" style="width: 160px">
          <el-option value="auto" label="自动" />
          <el-option value="1080p" label="1080P 高清" />
          <el-option value="720p" label="720P 高清" />
          <el-option value="480p" label="480P 清晰" />
          <el-option value="360p" label="360P 流畅" />
        </el-select>
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">自动切换画质</p>
          <p class="desc">根据网络状况动态调整清晰度</p>
        </div>
        <el-switch v-model="settings.autoQuality" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">硬件解码</p>
          <p class="desc">使用 GPU 加速解码，降低 CPU 占用</p>
        </div>
        <el-switch v-model="settings.hardwareDecode" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">预加载下一个视频</p>
          <p class="desc">连续播放时提前缓冲，切换更流畅</p>
        </div>
        <el-switch v-model="settings.preloadNext" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">缓存上限</p>
          <p class="desc">本地缓存最大占用（{{ settings.maxCacheMB }} MB）</p>
        </div>
        <el-slider
          v-model="settings.maxCacheMB"
          :min="100"
          :max="2000"
          :step="100"
          style="width: 220px"
        />
      </div>
    </div>

    <h4 class="section-title" style="margin: 28px 0 12px">缓存管理</h4>
    <el-card shadow="never" class="info-card">
      <div class="info-row">
        <span class="label">当前占用</span>
        <span class="value">{{ cacheUsedMB }} MB</span>
      </div>
      <div class="info-row">
        <span class="label">缓存上限</span>
        <span class="value">{{ settings.maxCacheMB }} MB</span>
      </div>
      <div class="info-row">
        <span class="label">操作</span>
        <el-button type="danger" plain size="small" :loading="clearing" @click="handleClearCache">
          清除缓存
        </el-button>
      </div>
    </el-card>

    <div class="footer-actions">
      <el-button @click="handleReset">恢复默认</el-button>
    </div>
  </div>
</template>

<style scoped>
@import './settings-common.css';

.info-card {
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--paper-white);
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--line-light);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: var(--mist);
  font-size: 14px;
}

.info-row .value {
  color: var(--ink);
  font-weight: 500;
}
</style>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useLocalSettings } from '@/composables/useLocalSettings'

const { settings, reset } = useLocalSettings('player', {
  autoplay: false,
  autoNext: true,
  rememberProgress: true,
  volume: 70,
  playbackRate: 1,
  danmakuEnabled: true,
  danmakuOpacity: 80,
  danmakuSpeed: 'normal' as 'slow' | 'normal' | 'fast',
  danmakuArea: 'full' as 'top' | 'half' | 'full',
  danmakuFontSize: 16
})

const handleReset = () => {
  reset()
}
</script>

<template>
  <div class="settings-panel">
    <h3 class="panel-title">播放设置</h3>
    <p class="panel-desc">控制视频自动播放、进度记忆与弹幕展示</p>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">自动播放</p>
          <p class="desc">进入视频详情页后自动开始播放</p>
        </div>
        <el-switch v-model="settings.autoplay" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">连续播放</p>
          <p class="desc">当前视频结束后自动播放下一个推荐视频</p>
        </div>
        <el-switch v-model="settings.autoNext" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">记忆播放进度</p>
          <p class="desc">下次打开同一视频时从上次位置继续</p>
        </div>
        <el-switch v-model="settings.rememberProgress" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">默认音量</p>
          <p class="desc">打开视频时的初始音量（{{ settings.volume }}%）</p>
        </div>
        <el-slider v-model="settings.volume" :min="0" :max="100" style="width: 220px" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">默认倍速</p>
          <p class="desc">新视频使用的默认播放速度</p>
        </div>
        <el-select v-model="settings.playbackRate" style="width: 140px">
          <el-option :value="0.5" label="0.5x" />
          <el-option :value="0.75" label="0.75x" />
          <el-option :value="1" label="1.0x（正常）" />
          <el-option :value="1.25" label="1.25x" />
          <el-option :value="1.5" label="1.5x" />
          <el-option :value="2" label="2.0x" />
        </el-select>
      </div>
    </div>

    <h4 class="section-title" style="margin: 28px 0 8px">弹幕</h4>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">显示弹幕</p>
          <p class="desc">播放器默认开启弹幕</p>
        </div>
        <el-switch v-model="settings.danmakuEnabled" />
      </div>
    </div>

    <div class="setting-block" :style="{ opacity: settings.danmakuEnabled ? 1 : 0.5 }">
      <div class="row">
        <div>
          <p class="title">弹幕透明度</p>
          <p class="desc">当前 {{ settings.danmakuOpacity }}%</p>
        </div>
        <el-slider
          v-model="settings.danmakuOpacity"
          :min="20"
          :max="100"
          :disabled="!settings.danmakuEnabled"
          style="width: 220px"
        />
      </div>
    </div>

    <div class="setting-block" :style="{ opacity: settings.danmakuEnabled ? 1 : 0.5 }">
      <div class="row">
        <div>
          <p class="title">弹幕字号</p>
          <p class="desc">{{ settings.danmakuFontSize }}px</p>
        </div>
        <el-slider
          v-model="settings.danmakuFontSize"
          :min="12"
          :max="28"
          :disabled="!settings.danmakuEnabled"
          style="width: 220px"
        />
      </div>
    </div>

    <div class="setting-block" :style="{ opacity: settings.danmakuEnabled ? 1 : 0.5 }">
      <div class="row">
        <div>
          <p class="title">弹幕速度</p>
          <p class="desc">滚动弹幕的移动速度</p>
        </div>
        <el-radio-group v-model="settings.danmakuSpeed" :disabled="!settings.danmakuEnabled">
          <el-radio-button value="slow">慢</el-radio-button>
          <el-radio-button value="normal">中</el-radio-button>
          <el-radio-button value="fast">快</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="setting-block" :style="{ opacity: settings.danmakuEnabled ? 1 : 0.5 }">
      <div class="row">
        <div>
          <p class="title">弹幕显示区域</p>
          <p class="desc">限制弹幕在屏幕中的显示范围</p>
        </div>
        <el-radio-group v-model="settings.danmakuArea" :disabled="!settings.danmakuEnabled">
          <el-radio-button value="top">顶部</el-radio-button>
          <el-radio-button value="half">半屏</el-radio-button>
          <el-radio-button value="full">全屏</el-radio-button>
        </el-radio-group>
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

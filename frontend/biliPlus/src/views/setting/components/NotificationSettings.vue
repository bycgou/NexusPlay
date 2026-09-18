<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useLocalSettings } from '@/composables/useLocalSettings'

const { settings, reset } = useLocalSettings('notify', {
  messageUnread: true,
  followUpdate: true,
  likeComment: true,
  systemNotice: true,
  liveStart: false,
  quietHours: false,
  quietStart: '23:00',
  quietEnd: '08:00'
})

const handleReset = () => {
  reset()
}
</script>

<template>
  <div class="settings-panel">
    <h3 class="panel-title">通知设置</h3>
    <p class="panel-desc">选择你希望接收的消息提醒类型</p>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">私信未读提醒</p>
          <p class="desc">有新私信时在顶栏显示红点</p>
        </div>
        <el-switch v-model="settings.messageUnread" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">关注动态</p>
          <p class="desc">关注的 UP 主发布新作品时提醒</p>
        </div>
        <el-switch v-model="settings.followUpdate" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">点赞与评论</p>
          <p class="desc">你的视频或评论被点赞、回复时提醒</p>
        </div>
        <el-switch v-model="settings.likeComment" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">系统通知</p>
          <p class="desc">审核结果、账号安全等平台消息</p>
        </div>
        <el-switch v-model="settings.systemNotice" />
      </div>
    </div>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">开播提醒</p>
          <p class="desc">关注的主播开播时发送提醒</p>
        </div>
        <el-switch v-model="settings.liveStart" />
      </div>
    </div>

    <h4 class="section-title" style="margin: 28px 0 8px">免打扰</h4>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">勿扰时段</p>
          <p class="desc">该时段内不推送非紧急通知</p>
        </div>
        <el-switch v-model="settings.quietHours" />
      </div>
    </div>

    <div
      v-if="settings.quietHours"
      class="setting-block"
    >
      <div class="row">
        <div>
          <p class="title">勿扰时间范围</p>
          <p class="desc">从开始时间到结束时间</p>
        </div>
        <div class="time-range">
          <el-time-select
            v-model="settings.quietStart"
            start="00:00"
            step="00:30"
            end="23:30"
            placeholder="开始"
            style="width: 120px"
          />
          <span class="sep">至</span>
          <el-time-select
            v-model="settings.quietEnd"
            start="00:00"
            step="00:30"
            end="23:30"
            placeholder="结束"
            style="width: 120px"
          />
        </div>
      </div>
    </div>

    <div class="footer-actions">
      <el-button @click="handleReset">恢复默认</el-button>
    </div>
  </div>
</template>

<style scoped>
@import './settings-common.css';

.time-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-range .sep {
  color: var(--mist);
  font-size: 13px;
}
</style>

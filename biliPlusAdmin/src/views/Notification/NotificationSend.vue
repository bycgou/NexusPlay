<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sendSystemNotification } from '@/api/notification'

const title = ref('')
const content = ref('')
const sending = ref(false)

const submit = async () => {
  const t = title.value.trim()
  const c = content.value.trim()
  if (!t) {
    ElMessage.warning('请填写通知标题')
    return
  }
  if (!c) {
    ElMessage.warning('请填写通知内容')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认向全站正常用户发送系统通知「${t}」？发送后无法撤回。`,
      '发送确认',
      { type: 'warning', confirmButtonText: '确认发送', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  sending.value = true
  try {
    await sendSystemNotification({ title: t, content: c })
    ElMessage.success('系统通知已发送')
    title.value = ''
    content.value = ''
  } catch (e) {
    // 接口异常已由 request 拦截器弹出提示，这里仅留调试信息
    console.error(e)
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <span class="card-title">发送系统通知</span>
      </template>

      <el-alert
          type="warning"
          :closable="false"
          show-icon
          title="通知会推送给全站状态正常的用户，请确认内容后再发送。"
          class="alert"
      />

      <div class="field">
        <label class="label">标题</label>
        <el-input v-model="title" maxlength="100" show-word-limit placeholder="例如：平台维护通知" />
      </div>

      <div class="field">
        <label class="label">内容</label>
        <el-input
            v-model="content"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="请输入通知内容，建议简明说明事由与生效时间"
        />
      </div>

      <div class="actions">
        <el-button type="primary" :loading="sending" @click="submit">发送通知</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page { padding: 16px 20px; }
.card { max-width: 720px; }
.card-title { font-weight: 600; }
.alert { margin-bottom: 16px; }
.field { margin-bottom: 16px; }
.label { display: block; margin-bottom: 6px; font-size: 13px; color: #606266; }
.actions { display: flex; justify-content: flex-end; }
</style>

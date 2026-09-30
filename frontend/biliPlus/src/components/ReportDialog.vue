<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAuthPrompt } from '@/composables/useAuthPrompt'
import { useRouter } from 'vue-router'
import { REPORT_REASONS, REPORT_TARGET, submitReport } from '@/api/report'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    /** 1视频 2评论 3弹幕 4用户 5直播间 */
    targetType: number
    targetId?: number
    title?: string
    /** 弹幕等需要手填 ID 的场景 */
    requireIdInput?: boolean
  }>(),
  {
    title: '举报',
    requireIdInput: false,
    targetId: undefined
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const userStore = useUserStore()
const router = useRouter()
const authPrompt = useAuthPrompt()

const reason = ref(1)
const detail = ref('')
const inputId = ref<number | undefined>(undefined)
const submitting = ref(false)

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    if (!userStore.userInfo?.id) {
      ElMessage.warning('请先登录后再举报')
      emit('update:modelValue', false)
      authPrompt.openLogin()
      return
    }
    reason.value = 1
    detail.value = ''
    inputId.value = props.targetId
  }
)

const resolveTargetId = () => {
  if (props.requireIdInput) {
    return Number(inputId.value)
  }
  return Number(props.targetId)
}

const handleSubmit = async () => {
  const targetId = resolveTargetId()
  if (!targetId || Number.isNaN(targetId)) {
    ElMessage.warning(props.requireIdInput ? '请填写要举报的 ID' : '缺少举报对象')
    return
  }
  submitting.value = true
  try {
    const res: any = await submitReport({
      targetType: props.targetType,
      targetId,
      reason: reason.value,
      detail: detail.value?.trim() || undefined
    })
    if (res?.code === 1) {
      ElMessage.success('举报已提交，我们会尽快处理')
      emit('update:modelValue', false)
      emit('success')
    } else {
      ElMessage.error(res?.msg || '举报失败')
    }
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

const typeLabel = () => {
  if (props.targetType === REPORT_TARGET.COMMENT) return '评论'
  if (props.targetType === REPORT_TARGET.DANMAKU) return '弹幕'
  if (props.targetType === REPORT_TARGET.USER) return '用户'
  if (props.targetType === REPORT_TARGET.LIVE_ROOM) return '直播间'
  return '视频'
}
</script>

<template>
  <el-dialog
      :model-value="modelValue"
      :title="title || `举报该${typeLabel()}`"
      width="480px"
      @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-width="70px">
      <el-form-item v-if="requireIdInput" :label="`${typeLabel()}ID`" required>
        <el-input-number v-model="inputId" :min="1" :controls="false" style="width: 100%" />
      </el-form-item>
      <el-form-item label="原因" required>
        <el-radio-group v-model="reason">
          <el-radio v-for="r in REPORT_REASONS" :key="r.value" :value="r.value">
            {{ r.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="补充">
        <el-input
            v-model="detail"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="补充说明（选填）"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">提交举报</el-button>
    </template>
  </el-dialog>
</template>

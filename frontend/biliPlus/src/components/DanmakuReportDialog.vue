<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAuthPrompt } from '@/composables/useAuthPrompt'
import { useRouter } from 'vue-router'
import { getVideoDanmakus } from '@/api/video'
import { REPORT_REASONS, REPORT_TARGET, submitReport } from '@/api/report'

interface DanmakuItem {
  id: number
  userId?: number
  text: string
  time?: number
  color?: string
}

const props = defineProps<{
  modelValue: boolean
  videoId: number | string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const userStore = useUserStore()
const router = useRouter()
const authPrompt = useAuthPrompt()

const loading = ref(false)
const list = ref<DanmakuItem[]>([])
const keyword = ref('')
const selectedId = ref<number | undefined>(undefined)
const reason = ref(1)
const detail = ref('')
const submitting = ref(false)

const formatTime = (sec?: number) => {
  const s = Math.max(0, Number(sec) || 0)
  const m = Math.floor(s / 60)
  const r = s % 60
  return `${String(m).padStart(2, '0')}:${String(r).padStart(2, '0')}`
}

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return list.value
  return list.value.filter((d) => (d.text || '').toLowerCase().includes(k))
})

const selectedItem = computed(() => list.value.find((d) => d.id === selectedId.value))

const loadList = async () => {
  if (!props.videoId) return
  loading.value = true
  try {
    const res: any = await getVideoDanmakus(Number(props.videoId), 500)
    if (res?.code === 1) {
      list.value = (res.data || []).map((d: any) => ({
        id: Number(d.id),
        userId: d.userId == null ? undefined : Number(d.userId),
        text: d.text || '',
        time: d.time,
        color: d.color
      }))
    } else {
      list.value = []
    }
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

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
    keyword.value = ''
    selectedId.value = undefined
    reason.value = 1
    detail.value = ''
    loadList()
  }
)

const pick = (item: DanmakuItem) => {
  selectedId.value = item.id
}

const handleSubmit = async () => {
  if (!selectedId.value) {
    ElMessage.warning('请先在列表中选择要举报的弹幕')
    return
  }
  submitting.value = true
  try {
    const res: any = await submitReport({
      targetType: REPORT_TARGET.DANMAKU,
      targetId: selectedId.value,
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
</script>

<template>
  <el-dialog
      :model-value="modelValue"
      title="举报弹幕"
      width="560px"
      @update:model-value="emit('update:modelValue', $event)"
  >
    <p class="tip">在下方列表中选择要举报的弹幕（可按内容搜索），无需知道 ID。</p>

    <el-input
        v-model="keyword"
        placeholder="搜索弹幕内容"
        clearable
        class="search"
    />

    <div v-loading="loading" class="dm-list">
      <el-empty
          v-if="!loading && !filtered.length"
          :description="list.length ? '没有匹配的弹幕' : '该视频暂无弹幕'"
          :image-size="64"
      />
      <div
          v-for="item in filtered"
          :key="item.id"
          class="dm-item"
          :class="{ active: selectedId === item.id }"
          @click="pick(item)"
      >
        <el-radio :model-value="selectedId" :value="item.id" class="dm-radio">
          <span class="dm-time">{{ formatTime(item.time) }}</span>
          <span class="dm-text">{{ item.text }}</span>
        </el-radio>
      </div>
    </div>

    <el-form label-width="70px" class="form">
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
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="补充说明（选填）"
        />
      </el-form-item>
    </el-form>

    <div v-if="selectedItem" class="preview">
      已选：{{ selectedItem.text }}
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="!selectedId" @click="handleSubmit">
        提交举报
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.tip {
  margin: 0 0 10px;
  font-size: 12px;
  color: var(--mist);
}
.search {
  margin-bottom: 8px;
}
.dm-list {
  max-height: 260px;
  overflow: auto;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--paper);
  margin-bottom: 12px;
}
.dm-item {
  padding: 6px 10px;
  border-bottom: 1px solid var(--line-light);
  cursor: pointer;
}
.dm-item:hover {
  background: var(--brand-bg);
}
.dm-item.active {
  background: var(--brand-bg);
}
.dm-radio {
  width: 100%;
  height: auto;
  white-space: normal;
}
.dm-time {
  display: inline-block;
  width: 48px;
  color: var(--mist);
  font-size: 12px;
  margin-right: 8px;
}
.dm-text {
  color: var(--ink-secondary);
  word-break: break-word;
}
.form {
  margin-top: 4px;
}
.preview {
  margin-top: 4px;
  font-size: 12px;
  color: var(--brand);
  word-break: break-word;
}
</style>

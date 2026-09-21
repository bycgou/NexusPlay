<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useLocalSettings } from '@/composables/useLocalSettings'

type ShortcutItem = {
  id: string
  label: string
  keys: string
  category: string
}

const { settings, reset } = useLocalSettings('shortcut', {
  enabled: true,
  disabledIds: [] as string[]
})

const listeningId = ref<string | null>(null)

const shortcuts: ShortcutItem[] = [
  { id: 'play', label: '播放 / 暂停', keys: 'Space', category: '播放控制' },
  { id: 'volumeUp', label: '音量增加', keys: '↑', category: '播放控制' },
  { id: 'volumeDown', label: '音量减少', keys: '↓', category: '播放控制' },
  { id: 'seekForward', label: '快进 5 秒', keys: '→', category: '播放控制' },
  { id: 'seekBack', label: '后退 5 秒', keys: '←', category: '播放控制' },
  { id: 'fullscreen', label: '全屏切换', keys: 'F', category: '播放控制' },
  { id: 'theater', label: '影院模式', keys: 'T', category: '播放控制' },
  { id: 'danmaku', label: '开关弹幕', keys: 'D', category: '播放控制' },
  { id: 'speed', label: '切换倍速', keys: 'Shift + →', category: '播放控制' },
  { id: 'next', label: '下一个视频', keys: 'N', category: '页面导航' },
  { id: 'home', label: '回到首页', keys: 'H', category: '页面导航' },
  { id: 'search', label: '聚焦搜索框', keys: '/', category: '页面导航' },
  { id: 'danmakuInput', label: '发送弹幕', keys: 'Enter', category: '弹幕与互动' },
  { id: 'like', label: '点赞当前视频', keys: 'L', category: '弹幕与互动' },
  { id: 'favorite', label: '收藏当前视频', keys: 'S', category: '弹幕与互动' }
]

const grouped = computed(() => {
  const map: Record<string, ShortcutItem[]> = {}
  shortcuts.forEach((item) => {
    if (!map[item.category]) map[item.category] = []
    map[item.category].push(item)
  })
  return map
})

const isDisabled = (id: string) => settings.disabledIds.includes(id)

const toggleShortcut = (id: string) => {
  if (!settings.enabled) return
  const idx = settings.disabledIds.indexOf(id)
  if (idx >= 0) settings.disabledIds.splice(idx, 1)
  else settings.disabledIds.push(id)
}

const startListen = (id: string) => {
  if (!settings.enabled) {
    ElMessage.warning('请先启用全局快捷键')
    return
  }
  listeningId.value = id
  ElMessage.info('请按下新的快捷键组合（Esc 取消）')
}

const formatEvent = (e: KeyboardEvent) => {
  const parts: string[] = []
  if (e.ctrlKey) parts.push('Ctrl')
  if (e.altKey) parts.push('Alt')
  if (e.shiftKey) parts.push('Shift')
  if (e.metaKey) parts.push('Meta')

  const key = e.key
  if (!['Control', 'Alt', 'Shift', 'Meta'].includes(key)) {
    if (key === ' ') parts.push('Space')
    else if (key.length === 1) parts.push(key.toUpperCase())
    else parts.push(key)
  }
  return parts.join(' + ')
}

const handleKeydown = (e: KeyboardEvent) => {
  if (!listeningId.value) return
  e.preventDefault()
  e.stopPropagation()
  if (e.key === 'Escape') {
    listeningId.value = null
    return
  }
  if (['Control', 'Alt', 'Shift', 'Meta'].includes(e.key)) return

  const combo = formatEvent(e)
  const target = shortcuts.find((s) => s.id === listeningId.value)
  if (target) {
    // 更新为自定义键（仅本地展示，播放器侧后续可读取）
    target.keys = combo
    ElMessage.success(`「${target.label}」已设为 ${combo}`)
  }
  listeningId.value = null
}

const handleReset = () => {
  reset()
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown, true)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown, true)
})
</script>

<template>
  <div class="settings-panel">
    <h3 class="panel-title">快捷键设置</h3>
    <p class="panel-desc">查看与调整播放器、页面导航快捷键</p>

    <div class="setting-block">
      <div class="row">
        <div>
          <p class="title">启用全局快捷键</p>
          <p class="desc">在播放页和站内页面启用键盘快捷键</p>
        </div>
        <el-switch v-model="settings.enabled" />
      </div>
    </div>

    <div
      v-for="(items, category) in grouped"
      :key="category"
      class="shortcut-group"
      :class="{ disabled: !settings.enabled }"
    >
      <h4 class="group-title">{{ category }}</h4>
      <div
        v-for="item in items"
        :key="item.id"
        class="shortcut-row"
      >
        <div class="info">
          <span class="label">{{ item.label }}</span>
          <el-tag v-if="isDisabled(item.id)" size="small" type="info">已停用</el-tag>
        </div>
        <div class="ops">
          <el-tag
            effect="plain"
            class="key-tag"
            :type="listeningId === item.id ? 'primary' : 'info'"
          >
            {{ listeningId === item.id ? '请按键…' : item.keys }}
          </el-tag>
          <el-button
            text
            type="primary"
            size="small"
            :disabled="!settings.enabled"
            @click="startListen(item.id)"
          >
            修改
          </el-button>
          <el-button
            text
            size="small"
            :disabled="!settings.enabled"
            @click="toggleShortcut(item.id)"
          >
            {{ isDisabled(item.id) ? '启用' : '停用' }}
          </el-button>
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

.shortcut-group {
  margin-top: 20px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--paper-white);
}

.shortcut-group.disabled {
  opacity: 0.55;
  pointer-events: none;
}

.group-title {
  margin: 0;
  padding: 12px 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-secondary);
  background: var(--paper);
  border-bottom: 1px solid var(--line-light);
}

.shortcut-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--line-light);
}

.shortcut-row:last-child {
  border-bottom: none;
}

.shortcut-row .info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shortcut-row .label {
  font-size: 14px;
  color: var(--ink);
}

.shortcut-row .ops {
  display: flex;
  align-items: center;
  gap: 4px;
}

.key-tag {
  min-width: 72px;
  justify-content: center;
  font-family: var(--font-mono);
  font-size: 12px;
}
</style>

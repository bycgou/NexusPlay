<!-- src/views/chat/components/ChatInput.vue -->
<template>
  <div class="chat-input">
    <div class="toolbar">
      <button type="button" class="tool-btn" title="发送图片" @click="onInsertImage">
        <el-icon size="18"><Picture /></el-icon>
      </button>
      <button type="button" class="tool-btn" title="表情" @click="toggleEmojiPicker">😊</button>
      <button type="button" class="tool-btn" title="视频通话" @click="startVideoCall">
        <el-icon size="18"><VideoCamera /></el-icon>
      </button>
      <span class="hint">Enter 发送 · Shift+Enter 换行</span>
    </div>

    <div class="input-row">
      <textarea
          ref="textareaRef"
          v-model="inputContent"
          class="input"
          rows="2"
          :placeholder="disabled ? '请选择会话' : '输入消息...'"
          :disabled="disabled"
          @keydown.enter.exact.prevent="handleSend"
      ></textarea>
      <button
          type="button"
          class="send-btn"
          :disabled="disabled || !inputContent.trim()"
          @click="handleSend"
      >发送</button>
    </div>

    <div v-if="showEmojiPicker" class="emoji-panel">
      <EmojiPicker :visible="true" @pick="handleEmojiPick" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Picture, VideoCamera } from '@element-plus/icons-vue'
import EmojiPicker from '@/views/components/EmojiPicker.vue'

defineProps({
  disabled: Boolean,
})

const emit = defineEmits(['send', 'insert-image', 'start-video-call'])

const inputContent = ref('')
const showEmojiPicker = ref(false)
const textareaRef = ref(null)

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const handleEmojiPick = (emoji) => {
  inputContent.value += emoji
  showEmojiPicker.value = false
  textareaRef.value?.focus()
}

const handleSend = () => {
  if (inputContent.value.trim()) {
    emit('send', inputContent.value)
    inputContent.value = ''
    showEmojiPicker.value = false
  }
}

const onInsertImage = () => {
  const url = prompt('请输入图片 URL')
  if (url) emit('insert-image', url)
}

const startVideoCall = () => emit('start-video-call')
</script>

<style scoped>
.chat-input {
  position: relative;
  flex-shrink: 0;
  background: #fff;
  border-top: 1px solid #eef0f4;
  padding: 10px 14px 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.tool-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #61666d;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.tool-btn:hover {
  background: #f0f2f5;
  color: #6c5ce7;
}

.hint {
  margin-left: auto;
  font-size: 11px;
  color: #c0c4cc;
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.input {
  flex: 1;
  min-height: 64px;
  max-height: 140px;
  resize: none;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.5;
  font-family: inherit;
  outline: none;
  background: #f7f8fa;
  color: #18191c;
  box-sizing: border-box;
}

.input:focus {
  border-color: #6c5ce7;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(108, 92, 231, 0.12);
}

.input:disabled {
  background: #f5f6f8;
  color: #c0c4cc;
}

.send-btn {
  height: 40px;
  min-width: 72px;
  padding: 0 18px;
  border: none;
  border-radius: 20px;
  background: #6c5ce7;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  flex-shrink: 0;
}

.send-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  background: #5a4bd1;
}

.emoji-panel {
  position: absolute;
  bottom: calc(100% - 4px);
  left: 14px;
  z-index: 30;
  width: 320px;
  max-width: calc(100vw - 40px);
}
</style>

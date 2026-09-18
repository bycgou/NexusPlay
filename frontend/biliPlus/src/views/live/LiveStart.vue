<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  startLive, stopLive, getMyLiveRoom, getStreamStatus, type LiveRoom
} from '@/api/live'
import { getCategories } from '@/api/category'
import { listMicPending } from '@/api/pk'
import { useLiveSocket } from '@/composables/useLiveSocket'
import ImageUploader from '@/components/ImageUploader.vue'

const router = useRouter()
const title = ref('')
const coverUrl = ref('')
const categoryId = ref<number | null>(null)
const categories = ref<any[]>([])
const liveRoom = ref<LiveRoom | null>(null)
const loading = ref(false)
const onlineCount = ref(0)
const chatMessages = ref<any[]>([])
const chatInput = ref('')
const micPending = ref<any[]>([])
const streamPublishing = ref(false)
const checkingStream = ref(false)
const localPreview = ref<HTMLVideoElement | null>(null)
const localStream = ref<MediaStream | null>(null)
const previewOn = ref(false)

let statusTimer: any = null

const { connect, disconnect, chat, micAccept } = useLiveSocket()

const obsServer = () => 'rtmp://localhost:1935/live'

const loadCategories = async () => {
  try {
    const res: any = await getCategories(2)
    categories.value = res?.data || []
  } catch (e) {
    console.error(e)
  }
}

const loadMyRoom = async () => {
  if (!localStorage.getItem('token')) return
  try {
    const res: any = await getMyLiveRoom()
    if (res?.data?.id) {
      liveRoom.value = res.data
      connectSocket()
      startStatusPoll()
    }
  } catch {
    /* 无进行中直播 */
  }
}

const handleStart = async () => {
  if (!title.value.trim()) {
    ElMessage.warning('请填写直播标题')
    return
  }
  if (!categoryId.value) {
    ElMessage.warning('请选择分区')
    return
  }
  loading.value = true
  try {
    const res: any = await startLive({
      title: title.value.trim(),
      coverUrl: coverUrl.value || undefined,
      categoryId: Number(categoryId.value)
    })
    liveRoom.value = res?.data
    ElMessage.success('开播成功，请用 OBS 推流')
    connectSocket()
    startStatusPoll()
  } catch {
    /* interceptor */
  } finally {
    loading.value = false
  }
}

const handleStop = async () => {
  if (!liveRoom.value?.id) return
  try {
    await ElMessageBox.confirm('确认结束本场直播？观众将无法继续观看。', '下播', { type: 'warning' })
  } catch {
    return
  }
  try {
    await stopLive(liveRoom.value.id)
    ElMessage.success('已下播')
    liveRoom.value = null
    streamPublishing.value = false
    stopStatusPoll()
    disconnect()
    stopLocalPreview()
  } catch (e) {
    console.error(e)
  }
}

const copyText = async (text: string, tip = '已复制') => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(tip)
  } catch {
    ElMessage.warning('复制失败，请手动选择')
  }
}

const startStatusPoll = () => {
  stopStatusPoll()
  checkStream()
  statusTimer = setInterval(checkStream, 5000)
}

const stopStatusPoll = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

const checkStream = async () => {
  if (!liveRoom.value?.id) return
  checkingStream.value = true
  try {
    const res: any = await getStreamStatus(liveRoom.value.id)
    streamPublishing.value = !!res?.data?.publishing
  } catch {
    /* ignore */
  } finally {
    checkingStream.value = false
  }
}

const toggleLocalPreview = async () => {
  if (previewOn.value) {
    stopLocalPreview()
    return
  }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
    localStream.value = stream
    previewOn.value = true
    await Promise.resolve()
    if (localPreview.value) {
      localPreview.value.srcObject = stream
      localPreview.value.muted = true
      await localPreview.value.play().catch(() => {})
    }
  } catch {
    ElMessage.warning('无法打开摄像头（可能被占用或无权限）')
  }
}

const stopLocalPreview = () => {
  localStream.value?.getTracks().forEach(t => t.stop())
  localStream.value = null
  previewOn.value = false
  if (localPreview.value) {
    localPreview.value.srcObject = null
  }
}

const connectSocket = () => {
  const token = localStorage.getItem('token')
  if (!token || !liveRoom.value?.id) return
  connect(token, liveRoom.value.id, {
    onOnline: (m) => {
      onlineCount.value = m.count || 0
      refreshMicPending()
    },
    onChat: (m) => {
      chatMessages.value.push(m)
      if (chatMessages.value.length > 80) chatMessages.value.shift()
    },
    onMicApply: (m) => {
      micPending.value.push(m)
    },
    onPkInvite: (m) => {
      ElMessage.info(`收到 PK 邀请（房间 ${m.fromRoomId}）`)
    }
  })
}

const refreshMicPending = async () => {
  if (!liveRoom.value?.id) return
  try {
    const res: any = await listMicPending(liveRoom.value.id)
    const list = res?.data || []
    micPending.value = list.map((s: any) => ({
      sessionId: s.id,
      userId: s.guestUserId,
      nickname: '观众' + s.guestUserId
    }))
  } catch {
    /* ignore */
  }
}

const acceptMic = (userId: number, agree: boolean) => {
  micAccept(userId, agree)
  micPending.value = micPending.value.filter(m => m.userId !== userId)
}

const sendChat = () => {
  const c = chatInput.value.trim()
  if (!c || !liveRoom.value?.id) return
  chat(liveRoom.value.id, c)
  chatInput.value = ''
}

const goRoomAsHost = () => {
  if (liveRoom.value?.id) {
    router.push(`/live/room/${liveRoom.value.id}`)
  }
}

onMounted(async () => {
  await loadCategories()
  await loadMyRoom()
})

onUnmounted(() => {
  stopStatusPoll()
  stopLocalPreview()
  disconnect()
})
</script>

<template>
  <div class="host-studio">
    <div class="studio-header">
      <h2>主播台</h2>
      <p v-if="!liveRoom" class="sub">填写信息开播，然后用 OBS 推流</p>
      <p v-else class="sub live">
        <span class="dot" :class="{ on: streamPublishing }"></span>
        {{ streamPublishing ? '推流正常，画面可被观众看到' : '等待 OBS 推流…' }}
      </p>
    </div>

    <!-- 未开播：设置 -->
    <div v-if="!liveRoom" class="card setup-card">
      <div class="setup-grid">
        <div class="form-side">
          <el-form label-width="72px" label-position="left">
            <el-form-item label="标题" required>
              <el-input v-model="title" maxlength="50" placeholder="给直播起个标题" show-word-limit />
            </el-form-item>
            <el-form-item label="分区" required>
              <el-select v-model="categoryId" placeholder="选择分区" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
              <div v-if="!categories.length" class="hint">暂无直播分区，请联系管理员在后台添加</div>
            </el-form-item>
            <el-form-item label="封面">
              <ImageUploader v-model="coverUrl" tip="上传封面" />
            </el-form-item>
          </el-form>
          <el-button type="primary" size="large" :loading="loading" @click="handleStart">
            开始直播
          </el-button>
        </div>
        <div class="preview-side">
          <div class="cam-box">
            <video ref="localPreview" playsinline muted class="cam-video"></video>
            <div v-if="!previewOn" class="cam-placeholder">本地预览未开启</div>
          </div>
          <el-button size="small" @click="toggleLocalPreview">
            {{ previewOn ? '关闭本地预览' : '打开本地预览（摄像头）' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 已开播：推流台 -->
    <div v-else class="card live-card">
      <div class="live-top">
        <div>
          <div class="room-title">{{ liveRoom.title }}</div>
          <div class="meta">
            房间 #{{ liveRoom.id }} · 在线 {{ onlineCount }}
            <el-tag size="small" :type="streamPublishing ? 'success' : 'warning'" style="margin-left:8px">
              {{ streamPublishing ? '已推流' : '未推流' }}
            </el-tag>
          </div>
        </div>
        <div class="top-actions">
          <el-button @click="checkStream" :loading="checkingStream">检测推流</el-button>
          <el-button @click="goRoomAsHost">进入直播间（主播模式）</el-button>
          <el-button type="danger" @click="handleStop">下播</el-button>
        </div>
      </div>

      <div class="live-grid">
        <div class="col-main">
          <div class="section-title">OBS 推流配置</div>
          <div class="obs-box">
            <div class="row">
              <div class="label">服务器</div>
              <code>{{ obsServer() }}</code>
              <el-button size="small" @click="copyText(obsServer(), '服务器已复制')">复制</el-button>
            </div>
            <div class="row">
              <div class="label">串流密钥</div>
              <code>{{ liveRoom.streamKey }}</code>
              <el-button size="small" @click="copyText(liveRoom.streamKey || '', '密钥已复制')">复制</el-button>
            </div>
            <div class="row">
              <div class="label">完整推流地址</div>
              <code>{{ liveRoom.pushUrl }}</code>
              <el-button size="small" @click="copyText(liveRoom.pushUrl || '', '推流地址已复制')">复制</el-button>
            </div>
            <div class="row">
              <div class="label">观众拉流地址</div>
              <code>{{ liveRoom.playUrl }}</code>
              <el-button size="small" @click="copyText(liveRoom.playUrl || '', '观看地址已复制')">复制</el-button>
            </div>
            <div class="obs-steps">
              <b>OBS 步骤：</b>
              ① 设置 → 推流 → 服务选「自定义」<br />
              ② 服务器粘贴上面「服务器」地址<br />
              ③ 串流密钥粘贴上面「串流密钥」（不要把整条 RTMP 都贴进服务器）<br />
              ④ 开始推流 → 上方状态变为「已推流」后观众才能看到画面
            </div>
          </div>

          <div class="section-title" style="margin-top:16px">本地预览</div>
          <div class="cam-box small">
            <video ref="localPreview" playsinline muted class="cam-video"></video>
            <div v-if="!previewOn" class="cam-placeholder">可开启摄像头自查画面（OBS 推流请用 OBS 自己的预览）</div>
          </div>
          <el-button size="small" style="margin-top:8px" @click="toggleLocalPreview">
            {{ previewOn ? '关闭本地预览' : '打开本地预览' }}
          </el-button>
        </div>

        <div class="col-side">
          <div class="section-title">弹幕互动</div>
          <div class="chat-box">
            <div class="msgs">
              <div v-for="(m, i) in chatMessages" :key="i" class="msg">
                <b>{{ m.nickname }}</b>：{{ m.content }}
              </div>
              <div v-if="!chatMessages.length" class="empty">暂无弹幕</div>
            </div>
            <div class="input-row">
              <el-input v-model="chatInput" placeholder="回复观众" size="small" @keyup.enter="sendChat" />
              <el-button size="small" type="primary" @click="sendChat">发送</el-button>
            </div>
          </div>

          <div v-if="micPending.length" class="mic-box">
            <div class="section-title">连麦申请</div>
            <div v-for="m in micPending" :key="m.userId" class="mic-item">
              <span>{{ m.nickname }}</span>
              <el-button size="small" type="primary" @click="acceptMic(m.userId, true)">同意</el-button>
              <el-button size="small" @click="acceptMic(m.userId, false)">拒绝</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.host-studio {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 16px 48px;
}
.studio-header h2 {
  margin: 0 0 4px;
  font-size: 22px;
}
.sub {
  margin: 0 0 16px;
  color: #666;
  font-size: 13px;
}
.sub.live {
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e6a23c;
  display: inline-block;
}
.dot.on {
  background: #67c23a;
  box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.25);
}
.card {
  background: var(--bg-card, #fff);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.setup-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 24px;
  align-items: start;
}
@media (max-width: 800px) {
  .setup-grid { grid-template-columns: 1fr; }
}
.hint {
  font-size: 12px;
  color: #e6a23c;
  margin-top: 4px;
}
.cam-box {
  position: relative;
  aspect-ratio: 16/9;
  background: #111;
  border-radius: 10px;
  overflow: hidden;
}
.cam-box.small {
  max-width: 420px;
}
.cam-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #111;
}
.cam-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  font-size: 13px;
  text-align: center;
  padding: 12px;
}
.live-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.room-title {
  font-size: 18px;
  font-weight: 700;
}
.meta {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}
.top-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.live-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 16px;
}
@media (max-width: 900px) {
  .live-grid { grid-template-columns: 1fr; }
}
.section-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
}
.obs-box {
  background: #f7f8fa;
  border-radius: 10px;
  padding: 12px;
}
.obs-box .row {
  display: grid;
  grid-template-columns: 90px 1fr auto;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
.obs-box .label {
  font-size: 12px;
  color: #666;
}
.obs-box code {
  word-break: break-all;
  font-size: 12px;
  background: #fff;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #eee;
}
.obs-steps {
  margin-top: 10px;
  font-size: 12px;
  color: #555;
  line-height: 1.7;
  background: #fff7e6;
  border-radius: 8px;
  padding: 10px 12px;
}
.chat-box {
  border: 1px solid #eee;
  border-radius: 10px;
  overflow: hidden;
}
.msgs {
  height: 220px;
  overflow-y: auto;
  padding: 10px;
  font-size: 13px;
}
.msg { margin-bottom: 6px; }
.empty { color: #aaa; text-align: center; margin-top: 40px; font-size: 12px; }
.input-row {
  display: flex;
  gap: 6px;
  padding: 8px;
  border-top: 1px solid #eee;
}
.mic-box {
  margin-top: 12px;
}
.mic-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 13px;
}
</style>

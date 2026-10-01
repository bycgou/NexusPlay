<template>
  <el-dialog
      v-model="visible"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      fullscreen
      custom-class="video-call-dialog"
      @closed="onDialogClose"
  >
    <!-- ========== 来电提醒（未接听前不建立媒体连接） ========== -->
    <div v-if="callPhase === 'incoming'" class="incoming-screen">
      <div class="incoming-avatar">
        <i class="fa fa-user"></i>
      </div>
      <h2 class="incoming-name">{{ contactName }}</h2>
      <p class="incoming-hint">邀请你进行视频通话…</p>

      <div class="incoming-actions">
        <div class="incoming-action">
          <el-button circle size="large" class="reject-btn" @click="rejectCall">
            <i class="fa fa-phone hangup-icon"></i>
          </el-button>
          <span class="action-label">拒绝</span>
        </div>
        <div class="incoming-action">
          <el-button circle size="large" class="accept-btn" @click="acceptCall">
            <i class="fa fa-phone"></i>
          </el-button>
          <span class="action-label">接听</span>
        </div>
      </div>
    </div>

    <!-- ========== 通话界面 ========== -->
    <template v-else>
      <!-- 远程视频区域 -->
      <div class="remote-video-container">
        <video
            ref="remoteVideo"
            class="remote-video"
            autoplay
            playsinline
        ></video>

        <!-- 占位提示 -->
        <div v-if="!remoteReady" class="remote-placeholder">
          <div class="user-avatar">
            <i class="fa fa-user"></i>
          </div>
          <p class="text-xl text-white mt-2">{{ contactName }}</p>
          <p class="text-gray-300 mt-1">{{ callStatus }}</p>
        </div>
      </div>

      <!-- 本地小窗 -->
      <div v-show="!!localStream" class="local-mini-video">
        <video
            ref="localVideo"
            class="local-video"
            autoplay
            muted
            playsinline
        ></video>
      </div>

      <!-- 控制按钮 -->
      <div class="control-buttons">
        <el-button
            circle
            size="large"
            class="hangup-btn"
            @click="hangup"
        >
          <i class="fa fa-phone hangup-icon"></i>
        </el-button>

        <el-button
            circle
            size="large"
            class="control-btn"
            @click="toggleMute"
        >
          <i :class="['fa', muted ? 'fa-microphone-slash' : 'fa-microphone']"></i>
        </el-button>

        <el-button
            circle
            size="large"
            class="control-btn"
            @click="toggleCamera"
        >
          <i :class="['fa', cameraOff ? 'fa-video-camera-slash' : 'fa-video-camera']"></i>
        </el-button>

        <el-dropdown
            v-if="videoDevices.length > 1"
            class="camera-select-btn"
            trigger="click"
            @command="switchCamera"
        >
          <el-button circle size="large" class="control-btn">
            <i class="fa fa-camera"></i>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                  v-for="device in videoDevices"
                  :key="device.deviceId || device.label"
                  :command="device"
              >
                {{ device.label || `摄像头 ${videoDevices.indexOf(device) + 1}` }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button
            circle
            size="large"
            class="control-btn"
            @click="toggleFullScreen"
        >
          <i class="fa fa-expand"></i>
        </el-button>
      </div>

      <!-- 顶部信息 -->
      <div class="call-info">
        <h2 class="text-white text-lg font-bold">{{ contactName }}</h2>
        <p class="text-gray-300 text-sm mt-1">{{ callStatus }}</p>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted, inject, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

type CallPhase = 'idle' | 'incoming' | 'calling' | 'connected'

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  contactName: { type: String, default: '联系人' },
  conversationId: { type: Number, required: true },
  targetUserId: { type: Number, required: true }
})

const emit = defineEmits(['update:modelValue', 'end-call'])
const visible = ref(false)

const chatSocket = inject<Ref<WebSocket | null>>('chatSocket', ref(null))

// ========== 状态 ==========
const localVideo = ref<HTMLVideoElement | null>(null)
const remoteVideo = ref<HTMLVideoElement | null>(null)
const localStream = ref<MediaStream | null>(null)
const remoteStream = ref<MediaStream | null>(null)
const remoteReady = ref(false)
const pc = ref<RTCPeerConnection | null>(null)

const callPhase = ref<CallPhase>('idle')
const muted = ref(false)
const cameraOff = ref(false)
const callStatus = ref('')
const isInitialized = ref(false)
const videoDevices = ref<MediaDeviceInfo[]>([])

// 未接听的来电信令
const incomingOffer = ref<WebRTCSignal | null>(null)

// ICE candidate 早于 PC 到达时缓存
const pendingCandidates: RTCIceCandidateInit[] = []
const pendingRemoteOffer = ref<WebRTCSignal | null>(null)

interface WebRTCSignal {
  type: 'offer' | 'answer' | 'candidate' | 'reject' | 'hangup'
  sdp?: string
  candidate?: RTCIceCandidateInit
}

// ========== 来电铃声（Web Audio 生成，无需外部文件） ==========
let audioCtx: AudioContext | null = null
let ringTimer: number | null = null

const startRingtone = () => {
  stopRingtone()
  try {
    const Ctx = window.AudioContext || (window as any).webkitAudioContext
    if (!Ctx) return
    audioCtx = new Ctx()
    const ctx = audioCtx
    // 被浏览器挂起时尝试恢复
    if (ctx.state === 'suspended') {
      ctx.resume().catch(() => {})
    }
    const beep = (freq: number, delay: number) => {
      const t0 = ctx.currentTime + delay
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.type = 'sine'
      osc.frequency.setValueAtTime(freq, t0)
      gain.gain.setValueAtTime(0, t0)
      gain.gain.linearRampToValueAtTime(0.22, t0 + 0.06)
      gain.gain.linearRampToValueAtTime(0, t0 + 0.42)
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.start(t0)
      osc.stop(t0 + 0.45)
    }
    const cycle = () => {
      beep(880, 0)
      beep(660, 0.5)
    }
    cycle()
    ringTimer = window.setInterval(cycle, 1800)
  } catch (e) {
    console.warn('铃声播放失败:', e)
  }
}

const stopRingtone = () => {
  if (ringTimer !== null) {
    clearInterval(ringTimer)
    ringTimer = null
  }
  if (audioCtx) {
    audioCtx.close().catch(() => {})
    audioCtx = null
  }
}

/** 页面上可能没有任何交互，浏览器会拦截声音；同时发系统通知兜底 */
const notifyIncoming = () => {
  try {
    if (typeof Notification === 'undefined') return
    if (Notification.permission === 'granted') {
      new Notification(`${props.contactName} 邀请你视频通话`, {
        body: '点击聊天窗口即可接听',
        tag: 'nexusplay-video-call'
      })
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission().catch(() => {})
    }
  } catch (e) {
    console.warn('系统通知发送失败:', e)
  }
}

// ========== 挂断 / 清理 ==========
const cleanup = () => {
  stopRingtone()
  if (localStream.value) {
    localStream.value.getTracks().forEach(t => t.stop())
    localStream.value = null
  }
  if (pc.value) {
    pc.value.close()
    pc.value = null
  }
  if (remoteVideo.value) remoteVideo.value.srcObject = null
  if (localVideo.value) localVideo.value.srcObject = null
  remoteStream.value = null
  remoteReady.value = false
  muted.value = false
  cameraOff.value = false
  isInitialized.value = false
  callPhase.value = 'idle'
  callStatus.value = ''
  pendingCandidates.length = 0
  pendingRemoteOffer.value = null
  incomingOffer.value = null
  videoDevices.value = []
}

/** 主动挂断：通知对端 */
const hangup = (notifyPeer = true) => {
  const wasActive = callPhase.value === 'calling' || callPhase.value === 'connected' || callPhase.value === 'incoming'
  if (notifyPeer && wasActive) {
    sendSignal({ type: 'hangup' })
  }
  cleanup()
  visible.value = false
  emit('update:modelValue', false)
  emit('end-call')
}

/** 拒接 */
const rejectCall = () => {
  sendSignal({ type: 'reject' })
  cleanup()
  visible.value = false
  emit('update:modelValue', false)
  emit('end-call')
  ElMessage.info('已拒绝通话')
}

// ========== visible 双向绑定 ==========
watch(
    () => props.modelValue,
    (newVal, oldVal) => {
      visible.value = newVal
      if (oldVal === true && newVal === false) {
        cleanup()
        emit('end-call')
      }
    },
    { immediate: true }
)

// ========== 远端视频绑定（关键：DOM 就绪后再挂流） ==========
watch(remoteStream, async (stream) => {
  if (!stream) {
    remoteReady.value = false
    return
  }
  await nextTick()
  const el = remoteVideo.value
  if (!el) return
  if (el.srcObject !== stream) {
    el.srcObject = stream
  }
  try {
    await el.play()
  } catch (e) {
    console.warn('远端视频自动播放被拦截，等待用户交互:', e)
  }
  remoteReady.value = true
})

// ========== 媒体与连接 ==========
const ensureMediaAvailable = (): boolean => {
  if (!window.isSecureContext) {
    ElMessage.error('请使用 https://www.nexusplay.website 打开后再试视频通话（当前是 HTTP）')
    return false
  }
  if (!navigator.mediaDevices || typeof navigator.mediaDevices.getUserMedia !== 'function') {
    ElMessage.error('当前浏览器不支持音视频通话，请改用 Chrome / Edge 桌面版，且不要用微信内置浏览器')
    return false
  }
  return true
}

const sendSignal = (signal: WebRTCSignal) => {
  const socket = chatSocket.value
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    console.warn('❌ WebSocket 未连接，无法发送信令')
    return
  }
  socket.send(JSON.stringify({
    conversationId: props.conversationId,
    msgType: 0,
    extraData: JSON.stringify(signal)
  }))
  console.log('🚀 发送 WebRTC 信令:', signal.type)
}

const createPeerConnection = () => {
  const peerConnection = new RTCPeerConnection({
    iceServers: [
      { urls: 'stun:stun.miwifi.com:3478' },
      { urls: 'stun:stun.chat.bilibili.com:3478' },
      { urls: 'stun:stun.l.google.com:19302' }
    ]
  })

  peerConnection.ontrack = (event) => {
    const stream = event.streams && event.streams[0]
    if (stream) {
      remoteStream.value = stream
    } else {
      if (!remoteStream.value) remoteStream.value = new MediaStream()
      remoteStream.value.addTrack(event.track)
    }
    callStatus.value = '通话中'
    callPhase.value = 'connected'
  }

  peerConnection.onicecandidate = (event) => {
    if (event.candidate) {
      sendSignal({ type: 'candidate', candidate: event.candidate })
    }
  }

  peerConnection.onconnectionstatechange = () => {
    const st = peerConnection.connectionState
    console.log('🔗 连接状态:', st)
    if (st === 'connected') {
      callStatus.value = '通话中'
      callPhase.value = 'connected'
    } else if (st === 'failed') {
      ElMessage.error('通话连接失败，可能是网络无法穿透')
      hangup()
    }
  }

  return peerConnection
}

const flushPendingCandidates = async () => {
  if (!pc.value) return
  const list = [...pendingCandidates]
  pendingCandidates.length = 0
  for (const c of list) {
    try {
      await pc.value.addIceCandidate(new RTCIceCandidate(c))
    } catch (e) {
      console.warn('addIceCandidate 失败:', e)
    }
  }
}

const getVideoDevices = async () => {
  try {
    const devices = await navigator.mediaDevices.enumerateDevices()
    videoDevices.value = devices.filter(d => d.kind === 'videoinput')
  } catch (err) {
    console.warn('无法获取摄像头列表:', err)
  }
}

const acquireLocalStream = async (preferVirtual = false): Promise<MediaStream> => {
  await getVideoDevices()
  const list = videoDevices.value
  let videoConstraints: boolean | MediaTrackConstraints = true

  if (preferVirtual) {
    const virtualCam = list.find(d => /obs|virtual/i.test(d.label))
    if (virtualCam) {
      videoConstraints = { deviceId: virtualCam.deviceId }
    }
  } else {
    const realCam = list.find(d => !/obs|virtual|fake/i.test(d.label))
    if (realCam) {
      videoConstraints = { deviceId: realCam.deviceId }
    } else if (list.length > 0) {
      videoConstraints = { deviceId: list[0].deviceId }
    }
  }
  if (list.length === 0) {
    videoConstraints = false
  }

  try {
    return await navigator.mediaDevices.getUserMedia({ video: videoConstraints, audio: true })
  } catch (err) {
    if (videoConstraints !== false) {
      console.warn('视频不可用，降级为仅音频:', err)
      ElMessage.warning('摄像头不可用，已切换为语音通话')
      cameraOff.value = true
      return await navigator.mediaDevices.getUserMedia({ video: false, audio: true })
    }
    throw err
  }
}

const bindLocalStream = (peerConnection: RTCPeerConnection, stream: MediaStream) => {
  localStream.value = stream
  nextTick(() => {
    if (localVideo.value) localVideo.value.srcObject = stream
  })
  stream.getTracks().forEach(track => peerConnection.addTrack(track, stream))
}

// ========== 主叫 ==========
const startOutgoingCall = async () => {
  if (isInitialized.value) return
  if (!ensureMediaAvailable()) {
    hangup(false)
    return
  }
  isInitialized.value = true
  callPhase.value = 'calling'
  callStatus.value = '正在呼叫…'

  const socket = chatSocket.value
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    ElMessage.error('聊天连接未就绪')
    hangup(false)
    return
  }

  try {
    const peerConnection = createPeerConnection()
    pc.value = peerConnection

    const stream = await acquireLocalStream(false)
    bindLocalStream(peerConnection, stream)

    const offer = await peerConnection.createOffer()
    await peerConnection.setLocalDescription(offer)
    sendSignal({ type: 'offer', sdp: offer.sdp })
    await flushPendingCandidates()
  } catch (err: any) {
    handleError(err)
    hangup(false)
  }
}

// ========== 被叫：收到来电，先响铃等人接听 ==========
const showIncomingCall = (offerSignal: WebRTCSignal) => {
  if (callPhase.value === 'connected' || isInitialized.value) return
  incomingOffer.value = offerSignal
  callPhase.value = 'incoming'
  visible.value = true
  startRingtone()
  notifyIncoming()
}

// ========== 被叫：点击接听，才开始采集摄像头 ==========
const acceptCall = async () => {
  const offer = incomingOffer.value
  if (!offer || !offer.sdp) return
  stopRingtone()

  if (!ensureMediaAvailable()) {
    sendSignal({ type: 'reject' })
    cleanup()
    visible.value = false
    emit('update:modelValue', false)
    emit('end-call')
    return
  }

  isInitialized.value = true
  callPhase.value = 'calling'
  callStatus.value = '正在接通…'

  const socket = chatSocket.value
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    ElMessage.error('聊天连接未就绪')
    sendSignal({ type: 'reject' })
    cleanup()
    visible.value = false
    emit('update:modelValue', false)
    emit('end-call')
    return
  }

  try {
    const peerConnection = createPeerConnection()
    pc.value = peerConnection

    await peerConnection.setRemoteDescription(new RTCSessionDescription(offer))
    await flushPendingCandidates()

    const stream = await acquireLocalStream(true)
    bindLocalStream(peerConnection, stream)

    const answer = await peerConnection.createAnswer()
    await peerConnection.setLocalDescription(answer)
    sendSignal({ type: 'answer', sdp: answer.sdp })
    await flushPendingCandidates()

    callStatus.value = '已接听…'
  } catch (err: any) {
    handleError(err)
    sendSignal({ type: 'reject' })
    cleanup()
    visible.value = false
    emit('update:modelValue', false)
    emit('end-call')
  }
}

// ========== 处理后续信令 ==========
const handleIncomingSignal = (signal: WebRTCSignal) => {
  // 对端拒绝 / 挂断
  if (signal.type === 'reject') {
    ElMessage.warning('对方已拒绝通话')
    cleanup()
    visible.value = false
    emit('update:modelValue', false)
    emit('end-call')
    return
  }
  if (signal.type === 'hangup') {
    ElMessage.info('对方已挂断')
    cleanup()
    visible.value = false
    emit('update:modelValue', false)
    emit('end-call')
    return
  }

  // 尚未接听时收到 offer：进入来电提醒
  if (signal.type === 'offer' && !isInitialized.value) {
    showIncomingCall(signal)
    return
  }

  // PC 未就绪：缓存候选
  if (!pc.value) {
    if (signal.type === 'candidate' && signal.candidate) {
      pendingCandidates.push(signal.candidate)
      return
    }
    if (signal.type === 'offer' && signal.sdp) {
      pendingRemoteOffer.value = signal
      return
    }
    return
  }

  try {
    if (signal.type === 'candidate' && signal.candidate) {
      pc.value.addIceCandidate(new RTCIceCandidate(signal.candidate))
    } else if (signal.type === 'answer' && signal.sdp) {
      pc.value.setRemoteDescription(new RTCSessionDescription(signal))
      callStatus.value = '通话中'
      callPhase.value = 'connected'
    } else if (signal.type === 'offer' && signal.sdp) {
      pc.value.setRemoteDescription(new RTCSessionDescription(signal))
    }
  } catch (err) {
    console.error('处理信令失败:', err)
  }
}

// ========== 错误处理 ==========
const handleError = (err: Error) => {
  console.error('WebRTC 错误:', err)
  let msg = '无法访问摄像头或麦克风'
  if (err.name === 'NotAllowedError') {
    msg = '请允许访问摄像头和麦克风'
  } else if (err.name === 'NotFoundError') {
    msg = '未检测到摄像头或麦克风'
  } else if (err.name === 'NotReadableError') {
    msg = '设备正被其他应用占用'
  } else if (!window.isSecureContext) {
    msg = 'HTTP 下无法使用摄像头，请用 HTTPS 或 localhost 访问'
  }
  ElMessage.error(msg)
}

// ========== 控制 ==========
const toggleMute = () => {
  muted.value = !muted.value
  localStream.value?.getAudioTracks().forEach(t => (t.enabled = !muted.value))
}

const toggleCamera = () => {
  cameraOff.value = !cameraOff.value
  localStream.value?.getVideoTracks().forEach(t => (t.enabled = !cameraOff.value))
}

const switchCamera = async (device: MediaDeviceInfo) => {
  if (!localStream.value || !pc.value) return
  try {
    const newStream = await navigator.mediaDevices.getUserMedia({
      video: { deviceId: { exact: device.deviceId } },
      audio: false
    })
    const newVideoTrack = newStream.getVideoTracks()[0]
    if (!newVideoTrack) return

    const sender = pc.value.getSenders().find(s => s.track?.kind === 'video')
    if (sender) await sender.replaceTrack(newVideoTrack)

    const audioTracks = localStream.value.getAudioTracks()
    const updated = new MediaStream([...audioTracks, newVideoTrack])
    if (localVideo.value) localVideo.value.srcObject = updated
    localStream.value.getVideoTracks().forEach(t => t.stop())
    localStream.value = updated
    ElMessage.success('已切换摄像头')
  } catch (err) {
    console.error('切换摄像头失败:', err)
    ElMessage.error('无法切换到该摄像头')
  }
}

const toggleFullScreen = () => {
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    document.documentElement.requestFullscreen().catch(console.warn)
  }
}

// ========== 生命周期 ==========
onUnmounted(() => {
  cleanup()
})

const onDialogClose = () => {
  if (callPhase.value !== 'idle') {
    hangup()
  }
}

defineExpose({
  startOutgoingCall,
  showIncomingCall,
  acceptCall,
  rejectCall,
  handleIncomingSignal
})
</script>

<style scoped>
:deep(.video-call-dialog .el-dialog) {
  background: transparent;
  box-shadow: none;
  margin: 0 !important;
  height: 100vh;
  width: 100vw;
}

:deep(.video-call-dialog .el-dialog__body) {
  padding: 0;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: #000;
}

/* ===== 来电界面 ===== */
.incoming-screen {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(160deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
}

.incoming-avatar {
  width: 112px;
  height: 112px;
  border-radius: 50%;
  background: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  animation: incoming-pulse 1.8s ease-in-out infinite;
}

.incoming-avatar i {
  font-size: 52px;
  color: #e2e8f0;
}

@keyframes incoming-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(96, 165, 250, 0.55); }
  50% { box-shadow: 0 0 0 18px rgba(96, 165, 250, 0); }
}

.incoming-name {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.incoming-hint {
  color: #cbd5e1;
  margin: 0;
  font-size: 14px;
}

.incoming-actions {
  display: flex;
  gap: 64px;
  margin-top: 40px;
}

.incoming-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.action-label {
  font-size: 13px;
  color: #cbd5e1;
}

.reject-btn {
  width: 64px;
  height: 64px;
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
}

.reject-btn:hover {
  background-color: #f78989;
}

.accept-btn {
  width: 64px;
  height: 64px;
  background-color: #22c55e;
  border-color: #22c55e;
  color: #fff;
  animation: accept-bounce 1.4s ease-in-out infinite;
}

.accept-btn:hover {
  background-color: #4ade80;
}

@keyframes accept-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.reject-btn i,
.accept-btn i {
  font-size: 24px;
}

/* ===== 通话界面 ===== */
.remote-video-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #000;
}

.remote-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #000;
}

.remote-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: white;
}

.user-avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background-color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.user-avatar i {
  font-size: 48px;
  color: white;
}

.local-mini-video {
  position: absolute;
  bottom: 16px;
  right: 16px;
  width: 128px;
  height: 192px;
  border: 2px solid rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  overflow: hidden;
  z-index: 10;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
}

.local-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.control-buttons {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 24px;
  z-index: 20;
}

.hangup-btn {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
  width: 56px;
  height: 56px;
}

.hangup-btn:hover {
  background-color: #f78989;
}

.hangup-icon {
  transform: rotate(135deg);
  display: block;
}

.control-btn {
  background-color: rgba(0, 0, 0, 0.6);
  border-color: transparent;
  color: white;
  width: 56px;
  height: 56px;
}

.control-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

:deep(.camera-select-btn .el-button) {
  background-color: rgba(0, 0, 0, 0.6);
  border-color: transparent;
  color: white;
  width: 56px;
  height: 56px;
}

:deep(.camera-select-btn .el-button:hover) {
  background-color: rgba(255, 255, 255, 0.2);
}

.call-info {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 20;
}

:deep(.control-btn i),
:deep(.hangup-btn i) {
  line-height: 1;
  font-size: 20px;
}
</style>

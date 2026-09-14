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
    <!-- 远程视频区域 -->
    <div class="remote-video-container">
      <video
          ref="remoteVideo"
          class="remote-video"
          autoplay
          playsinline
      ></video>

      <!-- 占位提示 -->
      <div v-if="!remoteStream" class="remote-placeholder">
        <div class="user-avatar">
          <i class="fa fa-user"></i>
        </div>
        <p class="text-xl text-white mt-2">{{ contactName }}</p>
        <p class="text-gray-300 mt-1">{{ callStatus }}</p>
      </div>
    </div>

    <!-- 本地小窗 -->
    <div class="local-mini-video">
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

      <!-- 🆕 新增：摄像头选择下拉 -->
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
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted, inject, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

// Props & Emits
const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  contactName: {
    type: String,
    default: '联系人'
  },
  conversationId: {
    type: Number,
    required: true
  },
  targetUserId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'end-call'])
const visible = ref(false)

// 注入 WebSocket
const chatSocket = inject<Ref<WebSocket | null>>('chatSocket', ref(null))

// ========== 挂断逻辑（必须先声明！）==========
const hangup = () => {
  if (localStream.value) {
    localStream.value.getTracks().forEach(t => t.stop())
    localStream.value = null
  }
  if (pc.value) {
    pc.value.close()
    pc.value = null
  }
  remoteStream.value = null
  muted.value = false
  cameraOff.value = false
  callStatus.value = '已挂断'
  visible.value = false
  emit('update:modelValue', false)
  emit('end-call')
}

// ========== 双向绑定 visible（安全调用 hangup）==========
watch(
    () => props.modelValue,
    (newVal, oldVal) => {
      visible.value = newVal
      if (oldVal === true && newVal === false) {
        hangup()
      }
    },
    { immediate: true }
)

// 视频引用
const localVideo = ref<HTMLVideoElement | null>(null)
const remoteVideo = ref<HTMLVideoElement | null>(null)

// 媒体流与连接
const localStream = ref<MediaStream | null>(null)
const remoteStream = ref<MediaStream | null>(null)
const pc = ref<RTCPeerConnection | null>(null)

// 状态控制
const muted = ref(false)
const cameraOff = ref(false)
const callStatus = ref('等待中...')
const isInitialized = ref(false)

// 🆕 新增：摄像头设备列表
const videoDevices = ref<MediaDeviceInfo[]>([])

// ========== 发送 WebRTC 信令 ==========
interface WebRTCSignal {
  type: 'offer' | 'answer' | 'candidate'
  sdp?: string
  candidate?: RTCIceCandidateInit
}

const sendSignal = (signal: WebRTCSignal) => {
  const socket = chatSocket.value
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    console.warn('❌ WebSocket 未连接，无法发送信令')
    return
  }

  const message = {
    conversationId: props.conversationId,
    msgType: 0,
    extraData: JSON.stringify(signal)
  }

  console.log('🚀 发送 WebRTC 信令:', signal)
  socket.send(JSON.stringify(message))
}

// ========== 初始化 PeerConnection（通用） ==========
const createPeerConnection = () => {
  const peerConnection = new RTCPeerConnection({
    iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
  })

  // 监听远端流
  peerConnection.ontrack = (event) => {
    remoteStream.value = event.streams[0]
    if (remoteVideo.value) remoteVideo.value.srcObject = remoteStream.value
    callStatus.value = '通话中'
  }

  // 监听 ICE 候选
  peerConnection.onicecandidate = (event) => {
    if (event.candidate) {
      sendSignal({ type: 'candidate', candidate: event.candidate })
    }
  }

  return peerConnection
}

// 🆕 新增：获取摄像头列表
const getVideoDevices = async () => {
  try {
    const devices = await navigator.mediaDevices.enumerateDevices()
    videoDevices.value = devices.filter(d => d.kind === 'videoinput')
  } catch (err) {
    console.warn('无法获取摄像头列表:', err)
  }
}

// 🆕 新增：切换摄像头
const switchCamera = async (device: MediaDeviceInfo) => {
  if (!localStream.value || !pc.value) return

  try {
    // 请求新视频流（仅视频）
    const newStream = await navigator.mediaDevices.getUserMedia({
      video: { deviceId: { exact: device.deviceId } },
      audio: false
    })

    const newVideoTrack = newStream.getVideoTracks()[0]
    if (!newVideoTrack) return

    // 替换 PeerConnection 中的视频轨道
    const sender = pc.value.getSenders().find(s => s.track?.kind === 'video')
    if (sender) {
      sender.replaceTrack(newVideoTrack)
    }

    // 更新本地预览（保留音频）
    const audioTracks = localStream.value.getAudioTracks()
    const updatedStream = new MediaStream([...audioTracks, newVideoTrack])
    if (localVideo.value) {
      localVideo.value.srcObject = updatedStream
    }

    // 停止旧视频轨道
    localStream.value.getVideoTracks().forEach(t => t.stop())

    // 更新本地流引用
    localStream.value = updatedStream

    ElMessage.success('已切换摄像头')
  } catch (err: any) {
    console.error('切换摄像头失败:', err)
    ElMessage.error('无法切换到该摄像头')
  }
}

// ========== 主叫：发起通话 ==========
const startOutgoingCall = async () => {
  if (isInitialized.value) return
  isInitialized.value = true

  const socket = chatSocket.value
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    ElMessage.error('聊天连接未就绪')
    hangup()
    return
  }

  try {
    callStatus.value = '正在获取摄像头...'

    // 🔍 获取所有摄像头
    await getVideoDevices()
    const videoDevicesList = videoDevices.value

    // 🎯 主叫策略：优先使用非虚拟摄像头
    const realCam = videoDevicesList.find(d => !/obs|virtual|fake/i.test(d.label))
    let videoConstraints: boolean | MediaTrackConstraints = true

    if (realCam) {
      console.log('🎥 主叫方使用真实摄像头:', realCam.label)
      videoConstraints = { deviceId: realCam.deviceId }
    } else if (videoDevicesList.length > 0) {
      console.warn('⚠️ 未找到明确的真实摄像头，使用默认设备:', videoDevicesList[0].label)
      videoConstraints = { deviceId: videoDevicesList[0].deviceId }
    } else {
      console.warn('❌ 无任何摄像头，仅启用音频')
      videoConstraints = false
    }

    // 📞 请求媒体流（带降级）
    let stream: MediaStream
    try {
      stream = await navigator.mediaDevices.getUserMedia({
        video: videoConstraints,
        audio: true
      })
    } catch (videoErr) {
      if (videoConstraints !== false) {
        console.warn('📹 主叫方视频不可用，降级为仅音频:', videoErr)
        ElMessage.warning('摄像头不可用，将以语音模式发起呼叫')
        stream = await navigator.mediaDevices.getUserMedia({
          video: false,
          audio: true
        })
        cameraOff.value = true
      } else {
        throw videoErr
      }
    }

    // 💾 绑定本地流
    localStream.value = stream
    if (localVideo.value) {
      localVideo.value.srcObject = stream
    }

    // 🔗 创建 PeerConnection 并添加轨道
    const peerConnection = createPeerConnection()
    pc.value = peerConnection
    stream.getTracks().forEach(track => peerConnection.addTrack(track, stream))

    // 📤 发送 offer
    callStatus.value = '正在呼叫...'
    const offer = await peerConnection.createOffer()
    await peerConnection.setLocalDescription(offer)
    sendSignal({ type: 'offer', sdp: offer.sdp })

  } catch (err: any) {
    handleError(err)
    hangup()
  }
}

// ========== 被叫：响应 offer ==========
const startIncomingCall = async (offerSignal: WebRTCSignal) => {
  if (isInitialized.value || !offerSignal.sdp) return
  isInitialized.value = true

  const socket = chatSocket.value
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    ElMessage.error('聊天连接未就绪')
    hangup()
    return
  }

  try {
    callStatus.value = '正在获取摄像头...'

    // 🔍 获取所有摄像头
    await getVideoDevices()
    const videoDevicesList = videoDevices.value

    // 🔎 被叫策略：优先使用 OBS 虚拟摄像头
    const obsCam = videoDevicesList.find(d => /obs|virtual/i.test(d.label))
    let videoConstraints: boolean | MediaTrackConstraints = true

    if (obsCam) {
      console.log('🎥 选用 OBS 虚拟摄像头:', obsCam.label)
      videoConstraints = { deviceId: obsCam.deviceId }
    } else if (videoDevicesList.length > 0) {
      console.log('⚠️ 未找到 OBS 虚拟摄像头，使用默认摄像头')
      videoConstraints = true
    } else {
      console.warn('❌ 无可用摄像头，仅启用音频')
      videoConstraints = false
    }

    // 📞 请求媒体流（带降级）
    let stream: MediaStream
    try {
      stream = await navigator.mediaDevices.getUserMedia({
        video: videoConstraints,
        audio: true
      })
    } catch (videoErr) {
      if (videoConstraints !== false) {
        console.warn('📹 视频不可用，降级为仅音频:', videoErr)
        ElMessage.warning('摄像头不可用，已切换为语音通话')
        stream = await navigator.mediaDevices.getUserMedia({
          video: false,
          audio: true
        })
        cameraOff.value = true
      } else {
        throw videoErr
      }
    }

    // 💾 绑定流
    localStream.value = stream
    if (localVideo.value) {
      localVideo.value.srcObject = stream
    }

    // 🔗 建立 PeerConnection
    const peerConnection = createPeerConnection()
    pc.value = peerConnection
    stream.getTracks().forEach(track => peerConnection.addTrack(track, stream))

    // 📥 设置远端描述（offer）
    await peerConnection.setRemoteDescription(new RTCSessionDescription(offerSignal))

    // 📤 创建并发送 answer
    const answer = await peerConnection.createAnswer()
    await peerConnection.setLocalDescription(answer)
    sendSignal({ type: 'answer', sdp: answer.sdp })

    callStatus.value = '已接听...'

  } catch (err: any) {
    handleError(err)
    hangup()
  }
}

// ========== 处理后续信令（candidate / answer） ==========
const handleIncomingSignal = (signal: WebRTCSignal) => {
  if (!pc.value) {
    console.warn('PeerConnection 未初始化，忽略信令:', signal)
    return
  }

  try {
    if (signal.type === 'candidate' && signal.candidate) {
      pc.value.addIceCandidate(new RTCIceCandidate(signal.candidate))
    } else if (signal.type === 'answer' && signal.sdp) {
      pc.value.setRemoteDescription(new RTCSessionDescription(signal))
      callStatus.value = '通话中'
    }
  } catch (err) {
    console.error('处理信令失败:', err)
    ElMessage.error('信令处理异常')
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
  }
  ElMessage.error(msg)
}

// ========== 控制方法 ==========
const toggleMute = () => {
  muted.value = !muted.value
  localStream.value?.getAudioTracks().forEach(t => (t.enabled = !muted.value))
}

const toggleCamera = () => {
  cameraOff.value = !cameraOff.value
  localStream.value?.getVideoTracks().forEach(t => (t.enabled = !cameraOff.value))
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
  hangup()
})

const onDialogClose = () => {
  if (visible.value) {
    hangup()
  }
}

// ========== 暴露方法给父组件 ==========
defineExpose({
  startOutgoingCall,
  startIncomingCall,
  handleIncomingSignal
})
</script>

<style scoped>
/* 样式保持不变 */
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
}

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

/* 🆕 新增：摄像头选择按钮样式 */
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

:deep(.video-call-dialog .el-dialog__wrapper),
:deep(.video-call-dialog .el-dialog) {
  z-index: 9999 !important;
}
:deep(.video-call-dialog .el-dialog__body) {
  padding: 0;
  height: 100vh;
  width: 100vw;
  position: relative;
  overflow: visible;
}

:deep(.hangup-icon) {
  transform: rotate(135deg);
  display: block;
}

:deep(.control-btn i),
:deep(.hangup-btn i) {
  line-height: 1;
  font-size: 20px;
}
</style>
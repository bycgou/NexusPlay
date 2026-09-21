<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import flvjs from 'flv.js'
import { getLiveRoom, enterLiveRoom, leaveLiveRoom, stopLive, listLiveRooms,
  getStreamStatus, type LiveRoom } from '@/api/live'
import { listGifts, getWallet, createRechargeOrder, payRechargeOrder,
  listWalletTransactions, sendGift, type Gift } from '@/api/gift'
import { getActivePk, respondPk, leaveMic, invitePk, endPk } from '@/api/pk'
import { useLiveSocket } from '@/composables/useLiveSocket'
import GiftEffectLayer from '@/components/GiftEffectLayer.vue'
import { SRS_FLV_BASE, SRS_FLV_PROXY_PREFIX } from '@/utils/env'

const route = useRoute()
const router = useRouter()
const roomId = Number(route.params.id)

const room = ref<LiveRoom | null>(null)
const onlineCount = ref(0)
const videoEl = ref<HTMLVideoElement | null>(null)
const guestVideoEl = ref<HTMLVideoElement | null>(null)
const opponentVideoEl = ref<HTMLVideoElement | null>(null)
let flvPlayer: any = null
let opponentPlayer: any = null

const chatMessages = ref<any[]>([])
const chatInput = ref('')
const gifts = ref<Gift[]>([])
const walletBalance = ref(0)
const giftCount = ref(1)
const selectedGift = ref<Gift | null>(null)

const giftBanner = ref<any>(null)
const giftStage = ref<any>(null)
/** 传给特效层的当前礼物（每次换对象触发 watch） */
const giftFx = ref<any>(null)
let giftTimer: any = null
let stageTimer: any = null
/** 特效串行队列，避免叠爆卡死 */
const giftQueue: any[] = []
let giftPlaying = false

const giftTier = ref(0) // 0全部 1 2 3

const giftGroups = computed(() => {
  const all = gifts.value || []
  return {
    all,
    l1: all.filter((g: any) => (g.effectLevel || 1) === 1),
    l2: all.filter((g: any) => g.effectLevel === 2),
    l3: all.filter((g: any) => g.effectLevel === 3)
  }
})

const displayGifts = computed(() => {
  const g = giftGroups.value
  if (giftTier.value === 1) return g.l1
  if (giftTier.value === 2) return g.l2
  if (giftTier.value === 3) return g.l3
  return g.all
})

const playerError = ref('')
const streamPublishing = ref(false)
/** flv 真正收到媒体数据（有画面）后为 true，用来去掉遮罩 */
const playerHasData = ref(false)
let statusTimer: any = null
let videoWatchTimer: any = null

/** 有画面帧就隐藏“等待推流”遮罩 */
const showWaitingOverlay = computed(() => {
  if (!room.value?.playUrl) return false
  return !streamPublishing.value && !playerHasData.value && !playerError.value
})
const showPlayerError = computed(() => {
  return !!playerError.value && !playerHasData.value
})

const stopVideoWatch = () => {
  if (videoWatchTimer) {
    clearInterval(videoWatchTimer)
    videoWatchTimer = null
  }
}

const watchVideoLive = () => {
  stopVideoWatch()
  videoWatchTimer = setInterval(() => {
    const v = videoEl.value as HTMLVideoElement | null
    if (v && v.videoWidth > 0 && v.videoHeight > 0) {
      playerHasData.value = true
      streamPublishing.value = true
      playerError.value = ''
    }
  }, 800)
}

const pk = reactive({
  active: false,
  pkId: 0 as number,
  scoreA: 0,
  scoreB: 0,
  opponentRoomId: 0,
  opponentName: '',
  opponentPlayUrl: '',
  durationSec: 300,
  leftSec: 300,
  isSideA: true
})
let pkTimer: any = null

const mic = reactive({
  active: false,
  role: '' as '' | 'host' | 'guest',
  rtcRoom: '',
  /** 后端是否配置了 RTC 媒体服务；false 时只走信令并明确提示不支持 */
  rtcAvailable: false as boolean | null,
  /** 是否已挂上远端媒体流（未挂上时展示文字状态而不是空白小窗） */
  hasMedia: false,
  pending: [] as any[]
})

const isLogin = () => !!localStorage.getItem('token')
const token = () => localStorage.getItem('token') || ''

const myUserId = computed(() => {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw) return null
    const u = JSON.parse(raw)
    return u?.id != null ? Number(u.id) : null
  } catch {
    return null
  }
})

/** 当前登录用户是否为该房主播 */
const isHost = computed(() => {
  if (!room.value || myUserId.value == null) return false
  return Number(room.value.userId) === myUserId.value
})

const {
  connect, disconnect, chat, micApply, micAccept, micLeave: wsMicLeave
} = useLiveSocket()

/** 把后端 playUrl 改写成走前端代理，避免浏览器直连 SRS 跨域 */
const toLocalPlayUrl = (url: string) => {
  if (!url) return ''
  // 后端 playUrl 与 VITE_SRS_FLV_BASE 同源时按配置替换，换 SRS 域名无需改代码
  if (SRS_FLV_BASE && url.startsWith(SRS_FLV_BASE)) {
    return SRS_FLV_PROXY_PREFIX + url.slice(SRS_FLV_BASE.length)
  }
  return url.replace(/^https?:\/\/[^/]+\/live\//, `${SRS_FLV_PROXY_PREFIX}/`)
}

const initPlayer = (rawUrl: string) => {
  if (!videoEl.value || !rawUrl) return
  destroyPlayer()
  playerError.value = ''
  playerHasData.value = false
  if (!flvjs.isSupported()) {
    playerError.value = '当前浏览器不支持 flv.js'
    return
  }
  const url = toLocalPlayUrl(rawUrl)
  flvPlayer = flvjs.createPlayer({
    type: 'flv',
    isLive: true,
    url
  }, {
    enableStashBuffer: false,
    stashInitialSize: 128,
    lazyLoad: false
  })
  flvPlayer.attachMediaElement(videoEl.value)
  flvPlayer.load()
  flvPlayer.play().catch(() => {
    /* 静音自动播一般成功；失败也不把整页当错误 */
  })
  const markHasData = () => {
    playerHasData.value = true
    streamPublishing.value = true
    playerError.value = ''
  }
  try {
    flvPlayer.on(flvjs.Events.MEDIA_INFO, markHasData)
    flvPlayer.on(flvjs.Events.METADATA_ARRIVED, markHasData)
    flvPlayer.on(flvjs.Events.SCRIPTDATA_ARRIVED, markHasData)
    flvPlayer.on(flvjs.Events.STATISTICS_INFO, () => {
      const v = videoEl.value as HTMLVideoElement | null
      if (v && v.videoWidth > 0) markHasData()
    })
  } catch {
    /* ignore */
  }
  if (videoEl.value) {
    const v = videoEl.value as HTMLVideoElement
    v.onloadeddata = markHasData
    v.oncanplay = markHasData
    v.onplaying = markHasData
  }
  flvPlayer.on(flvjs.Events.ERROR, () => {
    if (!playerHasData.value) {
      playerError.value = '拉流失败，请确认主播已推流'
    }
  })
  watchVideoLive()
}

const destroyPlayer = () => {
  stopVideoWatch()
  if (flvPlayer) {
    try { flvPlayer.pause(); flvPlayer.unload(); flvPlayer.detachMediaElement(); flvPlayer.destroy() } catch { /* ignore */ }
    flvPlayer = null
  }
}

const destroyOpponent = () => {
  if (opponentPlayer) {
    try { opponentPlayer.destroy() } catch { /* ignore */ }
    opponentPlayer = null
  }
}

const initOpponent = (url: string) => {
  destroyOpponent()
  if (!opponentVideoEl.value || !url || !flvjs.isSupported()) return
  opponentPlayer = flvjs.createPlayer({
    type: 'flv', isLive: true, url: toLocalPlayUrl(url)
  })
  opponentPlayer.attachMediaElement(opponentVideoEl.value)
  opponentPlayer.load()
  opponentPlayer.play().catch(() => {})
}

const showGift = (g: any) => {
  giftQueue.push(g)
  if (!giftPlaying) playNextGift()
}

const playNextGift = () => {
  const g = giftQueue.shift()
  if (!g) {
    giftPlaying = false
    return
  }
  giftPlaying = true
  // 每次新对象，触发 GiftEffectLayer watch
  giftFx.value = { ...g, _t: Date.now() }

  const lv = g.level || 1
  const holdMs = lv >= 3 ? 3600 : lv === 2 ? 2800 : 2400

  if (giftTimer) clearTimeout(giftTimer)
  giftTimer = setTimeout(() => {
    playNextGift()
  }, holdMs)
}

const sendGiftAction = async () => {
  if (!isLogin()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (isHost.value) {
    ElMessage.warning('主播不能给自己送礼')
    return
  }
  if (!selectedGift.value) {
    ElMessage.warning('请选择礼物')
    return
  }
  try {
    const res: any = await sendGift(roomId, selectedGift.value.id, giftCount.value)
    if (res?.data?.balance != null) walletBalance.value = res.data.balance
    ElMessage.success('赠送成功')
    // 本地立刻播特效（WS 广播也会再触发一次，队列去重由播放时长控制）
    const userRaw = localStorage.getItem('userInfo')
    let senderName = '我'
    try {
      const u = userRaw ? JSON.parse(userRaw) : {}
      senderName = u.nickname || u.username || '我'
    } catch { /* ignore */ }
    showGift({
      giftId: selectedGift.value.id,
      giftName: selectedGift.value.name,
      icon: selectedGift.value.iconUrl,
      senderName,
      count: giftCount.value,
      combo: res?.data?.combo || 1,
      level: selectedGift.value.effectLevel || 1
    })
  } catch {
    /* interceptor */
  }
}

/** 全屏特效分类（便于做不同动画） */
const giftFxClass = (g: any) => {
  const name = g?.giftName || ''
  if (name.includes('火箭')) return 'rocket'
  if (name.includes('嘉年华')) return 'carnival'
  if (name.includes('游艇')) return 'yacht'
  if (name.includes('宇宙') || name.includes('心')) return 'heart'
  if (name.includes('冠') || name.includes('王者')) return 'crown'
  if (name.includes('车')) return 'car'
  return 'default'
}

const doRecharge = async () => {
  let value: string
  try {
    const r = await ElMessageBox.prompt('充值硬币数量（本期为模拟支付）', '充值', {
      inputValue: '1000',
      inputPattern: /^\d+$/,
      inputErrorMessage: '请输入正整数'
    })
    value = r.value
  } catch {
    return // 用户取消
  }

  const amount = Number(value)
  try {
    // 先下单再支付：与真实支付网关同一条链路，支付成功才写账变
    const orderRes: any = await createRechargeOrder(amount)
    if (orderRes?.code !== 1 || !orderRes.data?.orderNo) {
      ElMessage.error(orderRes?.msg || '创建充值订单失败')
      return
    }
    await ElMessageBox.confirm(
      `应付金额 ¥${orderRes.data.payAmount}，确认支付？`,
      '模拟支付',
      { type: 'info', confirmButtonText: '确认支付', cancelButtonText: '取消' }
    )
    const payRes: any = await payRechargeOrder(orderRes.data.orderNo)
    if (payRes?.code !== 1) {
      ElMessage.error(payRes?.msg || '支付失败')
      return
    }
    const w: any = await getWallet()
    walletBalance.value = w?.data?.balance ?? walletBalance.value
    ElMessage.success('充值成功')
  } catch {
    /* 取消支付 */
  }
}

// ===== 我的账变明细 =====

const txVisible = ref(false)
const txLoading = ref(false)
const txList = ref<any[]>([])

const TX_TYPE_TEXT: Record<number, string> = {
  1: '充值',
  2: '送礼支出',
  3: '主播收入',
  4: '系统调整'
}

const openTransactions = async () => {
  txVisible.value = true
  txLoading.value = true
  try {
    const res: any = await listWalletTransactions({ size: 30 })
    txList.value = res?.data?.records || []
  } catch (e) {
    console.error('加载账变明细失败', e)
  } finally {
    txLoading.value = false
  }
}

const startStatusPoll = () => {
  stopStatusPoll()
  checkStream()
  statusTimer = setInterval(checkStream, 4000)
}

const stopStatusPoll = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

const checkStream = async () => {
  if (!room.value?.id || room.value.status !== 1) return
  try {
    const res: any = await getStreamStatus(room.value.id)
    const publishing = !!res?.data?.publishing
    const was = streamPublishing.value
    // 播放器已有画面时，即使接口偶发 false 也不回落遮罩
    if (playerHasData.value && !publishing) {
      streamPublishing.value = true
      return
    }
    streamPublishing.value = publishing
    if (publishing && !was && room.value?.playUrl && !playerHasData.value) {
      await nextTick()
      initPlayer(room.value.playUrl)
    }
  } catch {
    /* ignore */
  }
}

const loadPk = async () => {
  try {
    const res: any = await getActivePk(roomId)
    if (res?.data) {
      const p = res.data
      pk.active = true
      pk.pkId = p.id
      pk.scoreA = p.scoreA || 0
      pk.scoreB = p.scoreB || 0
      pk.isSideA = p.roomAId === roomId
      pk.opponentRoomId = pk.isSideA ? p.roomBId : p.roomAId
      const opp = await getLiveRoom(pk.opponentRoomId).catch(() => null) as any
      pk.opponentName = opp?.data?.hostNickname || '对手'
      pk.opponentPlayUrl = opp?.data?.playUrl || ''
      if (pk.opponentPlayUrl) nextTick(() => initOpponent(pk.opponentPlayUrl))
      startPkCountdown(p.durationSec || 300)
    }
  } catch {
    /* none */
  }
}

const startPkCountdown = (sec: number) => {
  if (pkTimer) clearInterval(pkTimer)
  pk.leftSec = sec
  pkTimer = setInterval(() => {
    pk.leftSec--
    if (pk.leftSec <= 0) clearInterval(pkTimer)
  }, 1000)
}

const handlePkInvite = async (m: any) => {
  if (!isHost.value) return
  try {
    await ElMessageBox.confirm(`收到 PK 邀请（房间 ${m.fromRoomId}），是否接受？`, 'PK 邀请', { type: 'info' })
    await respondPk(m.pkId, true)
  } catch {
    await respondPk(m.pkId, false).catch(() => {})
  }
}

const handlePkEnd = () => {
  pk.active = false
  if (pkTimer) clearInterval(pkTimer)
  destroyOpponent()
  ElMessage.info('PK 已结束')
}

const applyMicViaSocket = () => {
  if (!isLogin()) {
    ElMessage.warning('请先登录')
    return
  }
  if (isHost.value) {
    ElMessage.warning('主播无需申请连麦')
    return
  }
  micApply(roomId)
  ElMessage.success('已提交连麦申请')
}

const acceptMic = (userId: number, agree: boolean) => {
  micAccept(userId, agree)
  mic.pending = mic.pending.filter(p => p.userId !== userId)
}

const leaveMicAction = async () => {
  wsMicLeave(roomId)
  try { await leaveMic(roomId) } catch { /* ignore */ }
  mic.active = false
  mic.hasMedia = false
}

// ===== 主播发起 PK =====

const pkPickerVisible = ref(false)
const pkOpponents = ref<any[]>([])
const pkPicking = ref(false)
const pkInviting = ref(false)

const openPkPicker = async () => {
  pkPickerVisible.value = true
  pkPicking.value = true
  try {
    const res: any = await listLiveRooms(1, 50)
    const records = res?.data?.records || []
    // 排除自己房间，也不能和已在 PK 中的房间发起
    pkOpponents.value = records.filter((r: any) => Number(r.id) !== Number(roomId))
    if (!pkOpponents.value.length) {
      ElMessage.info('当前没有其他正在直播的房间')
    }
  } catch (e) {
    console.error('加载对手房间失败', e)
  } finally {
    pkPicking.value = false
  }
}

const confirmInvitePk = async (opponentRoomId: number) => {
  pkInviting.value = true
  try {
    const res: any = await invitePk(opponentRoomId)
    if (res?.code === 1) {
      ElMessage.success('PK 邀请已发出，等待对方同意')
      pkPickerVisible.value = false
    } else {
      ElMessage.error(res?.msg || '发起 PK 失败')
    }
  } catch (e) {
    console.error('发起 PK 失败', e)
  } finally {
    pkInviting.value = false
  }
}

const endPkAction = async () => {
  if (!pk.pkId) return
  try {
    await ElMessageBox.confirm('确认结束本次 PK？', 'PK', { type: 'warning' })
  } catch { return }
  try {
    await endPk(pk.pkId)
  } catch (e) {
    console.error('结束 PK 失败', e)
  }
}

const handleStopLive = async () => {
  if (!room.value?.id) return
  try {
    await ElMessageBox.confirm('确认结束本场直播？', '下播', { type: 'warning' })
  } catch { return }
  try {
    await stopLive(room.value.id)
    ElMessage.success('已下播')
    destroyPlayer()
    stopStatusPoll()
    room.value = null
    disconnect()
    router.push('/live/start')
  } catch (e) {
    console.error(e)
  }
}

const retryPlay = () => {
  if (room.value?.playUrl) initPlayer(room.value.playUrl)
}

const loadRoom = async () => {
  try {
    const res: any = await getLiveRoom(roomId)
    room.value = res?.data
    if (room.value?.playUrl) {
      await nextTick()
      initPlayer(room.value.playUrl)
    }
    startStatusPoll()
  } catch {
    ElMessage.error('房间不存在或未开播')
  }
}

const doEnter = async () => {
  if (!isLogin() || isHost.value) return
  try {
    const res: any = await enterLiveRoom(roomId)
    if (res?.data) room.value = res.data
  } catch {
    /* ignore */
  }
}

const doLeave = async () => {
  if (!isLogin() || isHost.value) return
  try { await leaveLiveRoom(roomId) } catch { /* ignore */ }
}

const connectWs = () => {
  if (!token()) return
  connect(token(), roomId, {
    onOnline: (m) => {
      onlineCount.value = m.count || 0
    },
    onChat: (m) => {
      chatMessages.value.push(m)
      if (chatMessages.value.length > 120) chatMessages.value.shift()
    },
    onGift: (m) => {
      showGift(m)
      chatMessages.value.push({
        nickname: m.senderName,
        content: `送出 ${m.giftName} x${m.count}${m.combo > 1 ? `（${m.combo} 连击）` : ''}`,
        type: 'gift'
      })
    },
    onPkStart: (m) => {
      if (m.roomId !== roomId && m.opponentRoomId !== roomId) return
      pk.active = true
      pk.pkId = m.pkId
      pk.opponentRoomId = m.opponentRoomId
      pk.opponentName = m.opponentName
      pk.opponentPlayUrl = m.opponentPlayUrl
      pk.durationSec = m.durationSec || 300
      nextTick(() => initOpponent(m.opponentPlayUrl))
      startPkCountdown(m.durationSec || 300)
    },
    onPkScore: (m) => {
      if (!pk.active || m.pkId !== pk.pkId) return
      pk.scoreA = m.scoreA
      pk.scoreB = m.scoreB
    },
    onPkEnd: handlePkEnd,
    onPkInvite: handlePkInvite,
    onPkCancel: () => ElMessage.info('PK 邀请已取消'),
    onMicApply: (m) => {
      if (isHost.value) mic.pending.push(m)
    },
    onMicReady: (m) => {
      mic.role = m.role || ''
      mic.rtcRoom = m.rtcRoom || ''
      mic.rtcAvailable = m.rtcAvailable === true
      mic.hasMedia = false

      if (mic.rtcAvailable) {
        // 信令与 token 已就绪；真正的音视频挂流需要 RTC 可用的 SRS
        mic.active = true
        ElMessage.success(m.role === 'host' ? '连麦已开始' : '已上麦')
      } else {
        // 未配置 RTC：不展示空白小窗，给出明确的降级提示
        mic.active = false
        ElMessage.warning('当前环境暂不支持连麦（未配置 RTC 媒体服务），已跳过音视频接入')
      }
    },
    onMicEnd: () => {
      mic.active = false
      mic.hasMedia = false
      ElMessage.info('连麦已结束')
    },
    onRoomClose: () => {
      ElMessage.warning('主播已下播')
      destroyPlayer()
      stopStatusPoll()
      room.value = null
    }
  })
}

const sendChatMsg = () => {
  const c = chatInput.value.trim()
  if (!c) return
  chat(roomId, c)
  chatInput.value = ''
}

const goSquare = () => router.push('/live')
const goStudio = () => router.push('/live/start')

onMounted(async () => {
  await loadRoom()
  await doEnter()
  await loadPk()
  connectWs()
  if (isLogin()) {
    try {
      const w: any = await getWallet()
      walletBalance.value = w?.data?.balance || 0
    } catch { /* ignore */ }
  }
  try {
    const g: any = await listGifts()
    gifts.value = g?.data || []
  } catch { /* ignore */ }
})

onUnmounted(() => {
  destroyPlayer()
  destroyOpponent()
  if (pkTimer) clearInterval(pkTimer)
  if (giftTimer) clearTimeout(giftTimer)
  stopStatusPoll()
  doLeave()
  disconnect()
})
</script>

<template>
  <div class="live-room">
    <div class="room-header">
      <div class="host" v-if="room">
        <el-avatar :src="room.hostAvatar || '/User.jpg'" />
        <div>
          <div class="title">
            {{ room.title }}
            <el-tag v-if="isHost" size="small" type="danger" style="margin-left:8px">主播模式</el-tag>
            <el-tag v-else size="small" type="info" style="margin-left:8px">观众</el-tag>
          </div>
          <div class="meta">
            {{ room.hostNickname || '主播' }} · 在线 {{ onlineCount }}
            <span class="stream-dot" :class="{ on: streamPublishing || playerHasData }"></span>
            <span class="stream-text">{{ (streamPublishing || playerHasData) ? '推流正常' : '等待推流' }}</span>
          </div>
        </div>
      </div>
      <div v-else class="host">
        <span>直播间</span>
      </div>
      <div class="header-actions">
        <el-button v-if="isHost" @click="goStudio">回主播台</el-button>
        <el-button v-if="isHost" type="danger" @click="handleStopLive">下播</el-button>
        <el-button text @click="goSquare">返回广场</el-button>
      </div>
    </div>

    <div v-if="pk.active" class="pk-bar">
      <div class="side">
        <span>{{ isHost ? '本房间' : 'A' }}</span>
        <b>{{ pk.scoreA }}</b>
      </div>
      <div class="center">
        <div>PK</div>
        <div class="timer">{{ Math.max(0, pk.leftSec) }}s</div>
      </div>
      <div class="side">
        <b>{{ pk.scoreB }}</b>
        <span>{{ pk.opponentName || '对手' }}</span>
      </div>
    </div>

    <div class="main-row">
      <div class="video-area" :class="{ 'pk-split': pk.active }">
        <div class="player-box">
          <video ref="videoEl" muted controls playsinline class="player"></video>
          <div v-if="!room?.playUrl" class="offline">房间已关闭或未开播</div>
          <div v-else-if="showWaitingOverlay" class="offline soft">
            正在连接直播流…<br />
            <span class="hint">若长时间无画面，请确认 OBS 已开始推流</span>
          </div>
          <div v-else-if="showPlayerError" class="offline">
            {{ playerError }}
            <el-button size="small" style="margin-left:12px" @click="retryPlay">重试播放</el-button>
          </div>
        </div>
        <div v-if="pk.active" class="player-box">
          <video ref="opponentVideoEl" muted playsinline class="player"></video>
          <div class="opp-label">{{ pk.opponentName }}</div>
        </div>
        <div v-if="mic.active" class="mic-window">
          <video v-show="mic.hasMedia" ref="guestVideoEl" muted playsinline class="player"></video>
          <div v-if="!mic.hasMedia" class="mic-waiting">连麦已建立，等待音视频接入…</div>
          <div class="mic-label">{{ mic.role === 'host' ? '连麦观众' : '主播' }}</div>
        </div>
      </div>

      <div class="chat-panel">
        <div class="msgs">
          <div v-for="(m, i) in chatMessages" :key="i" class="msg" :class="m.type">
            <b>{{ m.nickname }}：</b>{{ m.content }}
          </div>
          <div v-if="!chatMessages.length" class="empty-msg">说点什么吧…</div>
        </div>
        <div class="input-row">
          <el-input v-model="chatInput" placeholder="说点什么..." maxlength="200" @keyup.enter="sendChatMsg" />
          <el-button type="primary" @click="sendChatMsg">发送</el-button>
        </div>
        <div class="quick">
          <el-button size="small" text @click="chatInput = '666'">666</el-button>
          <el-button size="small" text @click="chatInput = '好听'">好听</el-button>
        </div>
      </div>
    </div>

    <div class="bottom-bar">
      <template v-if="!isHost">
        <div class="wallet">
          余额：<b>{{ walletBalance }}</b>
          <el-button size="small" text type="primary" @click="doRecharge">充值</el-button>
          <el-button size="small" text @click="openTransactions">明细</el-button>
        </div>
        <div class="gift-panel">
          <div class="gift-tabs">
            <span
                v-for="t in [{k:0,label:'全部'},{k:1,label:'一星'},{k:2,label:'二星'},{k:3,label:'三星'}]"
                :key="t.k"
                class="gift-tab"
                :class="{ on: giftTier === t.k }"
                @click="giftTier = t.k"
            >{{ t.label }}</span>
          </div>
          <div class="gift-scroll">
            <div
                v-for="g in displayGifts"
                :key="g.id"
                class="gift-item"
                :class="[{ active: selectedGift?.id === g.id }, 'lv' + (g.effectLevel || 1)]"
                @click="selectedGift = g"
            >
              <img v-if="g.iconUrl" :src="g.iconUrl" class="gift-icon" alt="" />
              <span class="gname">{{ g.name }}</span>
              <span class="gprice">{{ g.price }}</span>
            </div>
          </div>
          <div class="gift-send-bar">
            <span v-if="selectedGift" class="picked">已选 {{ selectedGift.name }} · {{ selectedGift.price }} 币</span>
            <span v-else class="picked dim">请选择礼物</span>
            <el-input-number v-model="giftCount" :min="1" :max="100" size="small" style="width: 100px" />
            <el-button type="primary" size="small" @click="sendGiftAction">赠送</el-button>
          </div>
        </div>
        <div class="actions">
          <el-button size="small" @click="applyMicViaSocket">申请连麦</el-button>
          <el-button v-if="mic.active" size="small" @click="leaveMicAction">下麦</el-button>
        </div>
      </template>
      <template v-else>
        <div class="host-bar">
          <span>主播控制台</span>
          <el-button size="small" @click="checkStream">检测推流</el-button>
          <el-button size="small" @click="retryPlay">刷新画面</el-button>
          <el-button size="small" @click="goStudio">主播台设置</el-button>
          <el-button
              v-if="!pk.active"
              size="small"
              type="primary"
              :disabled="!room?.id || room?.status !== 1"
              @click="openPkPicker"
          >
            发起 PK
          </el-button>
          <el-button v-else size="small" type="warning" @click="endPkAction">结束 PK</el-button>
          <el-button size="small" type="danger" @click="handleStopLive">下播</el-button>
        </div>
      </template>
    </div>

    <div v-if="isHost && mic.pending.length" class="mic-pending">
      <div v-for="m in mic.pending" :key="m.userId" class="mic-item">
        <span>{{ m.nickname || m.userId }} 申请连麦</span>
        <el-button size="small" type="primary" @click="acceptMic(m.userId, true)">同意</el-button>
        <el-button size="small" @click="acceptMic(m.userId, false)">拒绝</el-button>
      </div>
    </div>

    <!-- 发起 PK：选一个正在直播的对手房间 -->
    <el-dialog v-model="pkPickerVisible" title="选择 PK 对手" width="560px">
      <div v-loading="pkPicking" class="pk-picker">
        <el-empty v-if="!pkPicking && !pkOpponents.length" description="当前没有其他正在直播的房间" />
        <div v-for="o in pkOpponents" :key="o.id" class="pk-candidate">
          <img v-if="o.coverUrl" :src="o.coverUrl" class="pk-cover" alt="" />
          <div class="pk-info">
            <div class="pk-title">{{ o.title }}</div>
            <div class="pk-host">{{ o.hostNickname || ('主播' + o.userId) }}</div>
          </div>
          <el-button size="small" type="primary" :loading="pkInviting" @click="confirmInvitePk(o.id)">
            邀请 PK
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="pkPickerVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 我的账变明细 -->
    <el-dialog v-model="txVisible" title="我的账变明细" width="620px">
      <el-table v-loading="txLoading" :data="txList" size="small" empty-text="暂无账变记录">
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ TX_TYPE_TEXT[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column label="变动" width="100">
          <template #default="{ row }">
            <span :class="row.amount >= 0 ? 'tx-in' : 'tx-out'">
              {{ row.amount >= 0 ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" width="90" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="txVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 礼物特效层（15 款差异化动画） -->
    <GiftEffectLayer :gift="giftFx" />
  </div>
</template>

<style scoped>
.live-room {
  max-width: 1280px;
  margin: 0 auto;
  padding: 12px 16px 32px;
  position: relative;
}
.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 12px;
  flex-wrap: wrap;
}
.header-actions { display: flex; gap: 8px; }
.host { display: flex; align-items: center; gap: 10px; }
.title { font-weight: 600; font-size: 16px; display: flex; align-items: center; }
.meta { font-size: 12px; color: #888; display: flex; align-items: center; gap: 6px; }
.stream-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #ccc; display: inline-block;
}
.stream-dot.on { background: #67c23a; box-shadow: 0 0 6px #67c23a; }

.pk-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, #fb7299, #ff9a5b);
  color: #fff;
  border-radius: 8px;
  padding: 8px 16px;
  margin-bottom: 12px;
  font-weight: 600;
}
.pk-bar .center { text-align: center; }
.pk-bar .timer { font-size: 12px; font-weight: 400; opacity: 0.9; }

.main-row {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 12px;
  min-height: 360px;
}
.video-area {
  position: relative;
  display: grid;
  grid-template-columns: 1fr;
  gap: 4px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}
.video-area.pk-split { grid-template-columns: 1fr 1fr; }
.player-box {
  position: relative;
  aspect-ratio: 16/9;
  background: #111;
}
.mic-waiting {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
  font-size: 13px;
  text-align: center;
  padding: 0 12px;
}

/* 账变明细 */
.tx-in { color: #67c23a; }
.tx-out { color: #f56c6c; }

/* 发起 PK：对手选择 */
.pk-picker { min-height: 80px; }
.pk-candidate {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.pk-candidate:last-child { border-bottom: none; }
.pk-cover {
  width: 76px;
  height: 44px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
  background: #f5f7fa;
}
.pk-info { flex: 1; min-width: 0; }
.pk-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pk-host { font-size: 12px; color: #909399; }
.player {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}
.offline {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
  gap: 6px;
  text-align: center;
  pointer-events: none;
}
.offline:not(.soft) .el-button {
  pointer-events: auto;
}
.offline.soft {
  background: rgba(0, 0, 0, 0.35);
  color: #ddd;
}
.offline .hint { font-size: 12px; color: #777; }
.opp-label, .mic-label {
  position: absolute;
  left: 8px;
  top: 8px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}
.mic-window {
  position: absolute;
  right: 12px;
  bottom: 12px;
  width: 22%;
  min-width: 120px;
  aspect-ratio: 4/3;
  border: 2px solid #fff;
  border-radius: 6px;
  overflow: hidden;
  z-index: 2;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  background: var(--bg-card, #fff);
  border-radius: 8px;
  border: 1px solid rgba(0,0,0,0.06);
  min-height: 360px;
}
.msgs { flex: 1; overflow-y: auto; padding: 10px; font-size: 13px; }
.msg { margin-bottom: 6px; word-break: break-word; }
.msg.gift { color: #fb7299; }
.empty-msg { color: #bbb; font-size: 12px; padding: 12px; }
.input-row { display: flex; gap: 6px; padding: 8px; }
.quick { padding: 0 8px 8px; }

.bottom-bar {
  margin-top: 12px;
  background: var(--bg-card, #fff);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}
.host-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  font-size: 14px;
  font-weight: 600;
}
.gift-panel {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 220px;
  max-width: 420px;
  flex: 1;
}
.gift-tabs { display: flex; gap: 6px; }
.gift-tab {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  background: rgba(0,0,0,0.05);
  cursor: pointer;
  color: #666;
}
.gift-tab.on {
  background: #fb7299;
  color: #fff;
  font-weight: 600;
}
.gift-scroll {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding-bottom: 4px;
  max-height: 96px;
}
.gift-item {
  display: flex; flex-direction: column; align-items: center;
  gap: 2px; padding: 6px 8px; border-radius: 10px;
  border: 1px solid transparent; cursor: pointer; min-width: 68px;
  flex-shrink: 0;
  background: rgba(0,0,0,0.02);
  transition: transform 0.16s ease, background 0.16s ease, border-color 0.16s ease;
}
.gift-item:hover {
  transform: translateY(-2px);
  background: rgba(0,0,0,0.05);
}
.gift-item.lv2 {
  background: linear-gradient(180deg, rgba(255,154,91,0.12), rgba(255,154,91,0.04));
  border-color: rgba(255,154,91,0.25);
}
.gift-item.lv3 {
  background: linear-gradient(180deg, rgba(241,196,15,0.18), rgba(241,196,15,0.04));
  border-color: rgba(241,196,15,0.35);
}
.gift-item.active {
  border-color: #fb7299;
  background: rgba(251, 114, 153, 0.12);
}
.gift-item .gname { font-size: 12px; }
.gift-item .gprice { font-size: 11px; color: #f5a623; }
.gift-icon { width: 40px; height: 40px; object-fit: contain; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.15)); }
.gift-send-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.picked { font-size: 12px; color: #666; }
.picked.dim { color: #bbb; }
.wallet { font-size: 13px; }

.mic-pending {
  margin-top: 8px;
  background: #fff7e6;
  border-radius: 8px;
  padding: 8px 12px;
}
.mic-item {
  display: flex; align-items: center; gap: 8px;
  margin-top: 4px; font-size: 13px;
}

.gift-banner {
  position: fixed;
  left: 50%;
  bottom: 100px;
  transform: translateX(-50%);
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(90deg, #fb7299, #ff6b9d);
  color: #fff;
  padding: 10px 20px;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(251, 114, 153, 0.4);
  font-weight: 600;
}
.gift-banner.lv2 { background: linear-gradient(90deg, #ff9a5b, #ffb547); }
.gift-banner.lv3 {
  background: linear-gradient(90deg, #f5a623, #ff6b6b);
  animation: pulse 0.6s ease infinite alternate;
}
.gift-banner .icon { width: 28px; height: 28px; border-radius: 50%; }
.gift-banner .combo {
  background: rgba(0,0,0,0.25);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.gift-stage {
  position: fixed;
  inset: 0;
  z-index: 1100;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle, rgba(255,100,100,0.28), transparent 62%);
  animation: stageFadeIn 0.35s ease;
}
.gift-stage.fx-rocket {
  background: radial-gradient(circle at 50% 80%, rgba(231,76,60,0.45), rgba(41,128,185,0.2) 50%, transparent 70%);
}
.gift-stage.fx-carnival {
  background: radial-gradient(circle, rgba(245,166,35,0.4), rgba(255,107,107,0.2) 55%, transparent 70%);
}
.gift-stage.fx-yacht {
  background: radial-gradient(circle, rgba(52,152,219,0.4), rgba(236,240,241,0.12) 60%, transparent 70%);
}
.gift-stage.fx-heart {
  background: radial-gradient(circle, rgba(233,30,99,0.4), rgba(156,39,176,0.18) 60%, transparent 70%);
}
.gift-stage.fx-crown {
  background: radial-gradient(circle, rgba(241,196,15,0.45), rgba(243,156,18,0.2) 60%, transparent 70%);
}
.gift-stage .stage-content {
  text-align: center;
  color: #fff;
  text-shadow: 0 2px 16px rgba(0,0,0,0.55);
  animation: zoomIn 0.45s cubic-bezier(0.2, 0.9, 0.3, 1.2);
  z-index: 2;
}
.stage-icon-wrap {
  width: 96px;
  height: 96px;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: rgba(255,255,255,0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: iconFloat 1.2s ease-in-out infinite alternate;
}
.stage-icon { width: 72px; height: 72px; object-fit: contain; filter: drop-shadow(0 4px 12px rgba(0,0,0,0.35)); }
.stage-name { font-size: 22px; font-weight: 700; }
.stage-gift { font-size: 28px; margin: 8px 0; color: #ffd700; font-weight: 700; }
.stage-combo { font-size: 18px; color: #ff6b6b; font-weight: 700; }

.stage-particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.stage-particles i {
  position: absolute;
  left: calc(50% + (var(--i) - 6) * 6%);
  bottom: 10%;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ffd700;
  opacity: 0.85;
  animation: particleUp 2.4s ease-out infinite;
  animation-delay: calc(var(--i) * 0.12s);
}
.gift-stage.fx-rocket .stage-particles i {
  background: #ff7675;
  width: 6px;
  height: 18px;
  border-radius: 3px;
}
.gift-stage.fx-yacht .stage-particles i { background: #74b9ff; }
.gift-stage.fx-heart .stage-particles i { background: #fd79a8; }
.gift-stage.fx-carnival .stage-particles i {
  background: hsl(calc(var(--i) * 28deg), 90%, 60%);
}

@keyframes stageFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes iconFloat {
  from { transform: translateY(0) scale(1); }
  to { transform: translateY(-10px) scale(1.06); }
}
@keyframes particleUp {
  0% { transform: translateY(0) scale(0.6); opacity: 0.9; }
  100% { transform: translateY(-55vh) scale(1.2); opacity: 0; }
}

.banner-enter-active { animation: slideUp 0.3s ease; }
.banner-leave-active { animation: slideUp 0.25s reverse; }
@keyframes slideUp {
  from { opacity: 0; transform: translateX(-50%) translateY(20px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}
@keyframes pulse {
  from { transform: translateX(-50%) scale(1); }
  to { transform: translateX(-50%) scale(1.04); }
}
@keyframes zoomIn {
  from { transform: scale(0.6); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

@media (max-width: 900px) {
  .main-row { grid-template-columns: 1fr; }
}
</style>

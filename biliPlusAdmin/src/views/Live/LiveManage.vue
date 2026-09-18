<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import flvjs from 'flv.js'
import {
  getLiveRooms,
  getLiveRoomDetail,
  forceStopLive,
  banLiveHost,
  type LiveRoom
} from '@/api/live'

const loading = ref(false)
const list = ref<LiveRoom[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const statusFilter = ref<number | ''>('')

// 预览弹窗
const previewVisible = ref(false)
const previewRoom = ref<LiveRoom | null>(null)
const videoEl = ref<HTMLVideoElement | null>(null)
let player: any = null
const playerMsg = ref('')

const statusText = (s?: number) => {
  if (s === 1) return '直播中'
  if (s === 2) return '已结束'
  return '未开播'
}

const toLocalPlayUrl = (url: string) => {
  if (!url) return ''
  return url.replace(/^https?:\/\/[^/]+\/live\//, '/srs-live/')
}

const destroyPlayer = () => {
  if (player) {
    try {
      player.pause()
      player.unload()
      player.detachMediaElement()
      player.destroy()
    } catch {
      /* ignore */
    }
    player = null
  }
}

const initPlayer = (rawUrl: string) => {
  destroyPlayer()
  playerMsg.value = ''
  if (!videoEl.value || !rawUrl) return
  if (!flvjs.isSupported()) {
    playerMsg.value = '当前浏览器不支持 flv.js'
    return
  }
  const url = toLocalPlayUrl(rawUrl)
  player = flvjs.createPlayer(
    { type: 'flv', isLive: true, url },
    { enableStashBuffer: false, lazyLoad: false }
  )
  player.attachMediaElement(videoEl.value)
  player.load()
  player.play().catch(() => {})
  player.on(flvjs.Events.ERROR, () => {
    playerMsg.value = '拉流失败，主播可能未推流'
  })
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getLiveRooms({
      status: statusFilter.value === '' ? undefined : Number(statusFilter.value),
      page: page.value,
      size: size
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

/** 进入直播间（管理预览） */
const openPreview = async (row: LiveRoom) => {
  try {
    const res = await getLiveRoomDetail(row.id)
    previewRoom.value = res.data || row
  } catch {
    previewRoom.value = row
  }
  previewVisible.value = true
  await nextTick()
  if (previewRoom.value?.playUrl) {
    initPlayer(previewRoom.value.playUrl)
  } else {
    playerMsg.value = '该房间暂无拉流地址（可能已下播）'
  }
}

const closePreview = () => {
  previewVisible.value = false
  destroyPlayer()
  previewRoom.value = null
  loadList()
}

const handleForceStop = async (row: LiveRoom) => {
  try {
    await ElMessageBox.confirm(`确认强制下播「${row.title}」？`, '提示', { type: 'warning' })
    await forceStopLive(row.id)
    ElMessage.success('已强制下播')
    if (previewVisible.value && previewRoom.value?.id === row.id) {
      destroyPlayer()
    }
    await loadList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

/** 封禁：强制下播 + 禁用主播账号 */
const handleBan = async (row: LiveRoom) => {
  try {
    await ElMessageBox.confirm(
      `确认封禁主播「${row.hostNickname || row.userId}」？\n将强制下播并禁用该账号（status=0）。`,
      '违规封禁',
      { type: 'warning', confirmButtonText: '确认封禁', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  try {
    await banLiveHost(row.id)
    ElMessage.success('已强制下播并封禁主播')
    if (previewVisible.value && previewRoom.value?.id === row.id) {
      destroyPlayer()
    }
    await loadList()
  } catch (e) {
    console.error(e)
  }
}

const openUserRoom = (row: LiveRoom) => {
  window.open(`/live/room/${row.id}`, '_blank')
}

const onFilter = () => {
  page.value = 1
  loadList()
}

onMounted(loadList)
onUnmounted(destroyPlayer)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 140px" @change="onFilter">
        <el-option label="直播中" :value="1" />
        <el-option label="已结束" :value="2" />
        <el-option label="未开播" :value="0" />
      </el-select>
      <el-button @click="onFilter">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
      <el-table-column prop="hostNickname" label="主播" width="110" show-overflow-tooltip />
      <el-table-column prop="userId" label="主播ID" width="90" />
      <el-table-column prop="onlineCount" label="在线" width="70" />
      <el-table-column prop="viewCount" label="观看" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'danger' : 'info'" size="small">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开播时间" width="150" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button
              v-if="row.status === 1"
              size="small"
              type="primary"
              @click="openPreview(row)"
          >进入直播间</el-button>
          <el-button
              v-if="row.status === 1"
              size="small"
              type="warning"
              @click="handleForceStop(row)"
          >强制下播</el-button>
          <el-button
              v-if="row.status === 1"
              size="small"
              type="danger"
              @click="handleBan(row)"
          >封禁</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadList"
      />
    </div>

    <!-- 直播预览弹窗 -->
    <el-dialog
        v-model="previewVisible"
        title="直播预览（监管）"
        width="920px"
        destroy-on-close
        @close="closePreview"
    >
      <div v-if="previewRoom" class="preview-body">
        <div class="preview-meta">
          <div>
            <div class="ptitle">{{ previewRoom.title }}</div>
            <div class="pmeta">
              房间 #{{ previewRoom.id }} ·
              主播 {{ previewRoom.hostNickname || previewRoom.userId }} ·
              ID {{ previewRoom.userId }} ·
              在线 {{ previewRoom.onlineCount ?? 0 }}
            </div>
          </div>
          <div class="p-actions">
            <el-button size="small" @click="openUserRoom(previewRoom)">新窗口打开前台</el-button>
            <el-button size="small" type="warning" @click="handleForceStop(previewRoom)">强制下播</el-button>
            <el-button size="small" type="danger" @click="handleBan(previewRoom)">封禁主播</el-button>
          </div>
        </div>

        <div class="player-wrap">
          <video ref="videoEl" muted controls playsinline class="player"></video>
          <div v-if="playerMsg" class="player-msg">{{ playerMsg }}</div>
        </div>

        <div class="play-url" v-if="previewRoom.playUrl">
          拉流：{{ previewRoom.playUrl }}
        </div>
      </div>
      <template #footer>
        <el-button @click="closePreview">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
.preview-body { display: flex; flex-direction: column; gap: 12px; }
.preview-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}
.ptitle { font-weight: 600; font-size: 16px; }
.pmeta { font-size: 12px; color: #888; margin-top: 4px; }
.p-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.player-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}
.player { width: 100%; height: 100%; object-fit: contain; background: #000; }
.player-msg {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  background: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}
.play-url { font-size: 12px; color: #999; word-break: break-all; }
</style>

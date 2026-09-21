<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import flvjs from 'flv.js'
import { listLiveRooms, listLiveReplays, type LiveRoom, type LiveReplay } from '@/api/live'
import { SRS_FLV_BASE, SRS_FLV_PROXY_PREFIX } from '@/utils/env'
import { VideoCameraFilled } from '@element-plus/icons-vue'

const router = useRouter()

const activeTab = ref<'live' | 'replay'>('live')

const rooms = ref<LiveRoom[]>([])
const total = ref(0)
const page = ref(1)
const size = 12
const loading = ref(false)

const replays = ref<LiveReplay[]>([])
const replayTotal = ref(0)
const replayPage = ref(1)
const replayLoading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res: any = await listLiveRooms(page.value, size)
    rooms.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadReplays = async () => {
  replayLoading.value = true
  try {
    const res: any = await listLiveReplays({ page: replayPage.value, size })
    replays.value = res?.data?.records || []
    replayTotal.value = res?.data?.total || 0
  } catch (e) {
    console.error('加载回放失败', e)
  } finally {
    replayLoading.value = false
  }
}

const goRoom = (id: number) => {
  router.push(`/live/room/${id}`)
}

const goStart = () => {
  router.push('/live/start')
}

const formatDuration = (sec?: number) => {
  const total = Math.max(0, Math.floor(Number(sec) || 0))
  if (!total) return ''
  const m = Math.floor(total / 60)
  const s = total % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

// ===== 回放播放：flv 走 flv.js，其余交给原生 video =====

const replayVisible = ref(false)
const replayCurrent = ref<LiveReplay | null>(null)
const replayVideoEl = ref<HTMLVideoElement | null>(null)
const replayError = ref('')
let flvPlayer: any = null

/** 与 LiveRoom 一致：后端点播地址经前端代理访问，避免 SRS 跨域 */
const toProxyUrl = (url: string) => {
  if (!url) return ''
  if (SRS_FLV_BASE && url.startsWith(SRS_FLV_BASE)) {
    return SRS_FLV_PROXY_PREFIX + url.slice(SRS_FLV_BASE.length)
  }
  return url
}

const destroyReplayPlayer = () => {
  if (flvPlayer) {
    try { flvPlayer.destroy() } catch { /* ignore */ }
    flvPlayer = null
  }
  if (replayVideoEl.value) {
    try { replayVideoEl.value.removeAttribute('src') } catch { /* ignore */ }
  }
}

const openReplay = async (item: LiveReplay) => {
  replayCurrent.value = item
  replayError.value = ''
  replayVisible.value = true
  await nextTick()
  attachReplaySource(item.playUrl)
}

const attachReplaySource = (rawUrl: string) => {
  const el = replayVideoEl.value
  if (!el || !rawUrl) return
  destroyReplayPlayer()
  const url = toProxyUrl(rawUrl)
  const isFlv = /\.flv(\?|$)/i.test(url)

  if (isFlv) {
    if (!flvjs.isSupported()) {
      replayError.value = '当前浏览器不支持 flv 回放'
      return
    }
    flvPlayer = flvjs.createPlayer({ type: 'flv', url, isLive: false })
    flvPlayer.attachMediaElement(el)
    flvPlayer.load()
    flvPlayer.play().catch(() => { /* 需要用户手势，忽略 */ })
    flvPlayer.on(flvjs.Events.ERROR, () => {
      replayError.value = '回放加载失败，可能录制文件已过期'
    })
  } else {
    el.src = url
    el.play().catch(() => { /* 需要用户手势，忽略 */ })
  }
}

const closeReplay = () => {
  destroyReplayPlayer()
  replayCurrent.value = null
}

const handleTabChange = (name: string | number) => {
  if (name === 'replay' && replays.value.length === 0) {
    loadReplays()
  }
}

onMounted(() => {
  load()
  loadReplays()
})

onBeforeUnmount(destroyReplayPlayer)
</script>

<template>
  <div class="live-square">
    <div class="page-header">
      <div>
        <h2>直播广场</h2>
        <p class="sub">
          <template v-if="activeTab === 'live'">正在直播 {{ total }} 个房间</template>
          <template v-else>共 {{ replayTotal }} 个回放</template>
        </p>
      </div>
      <el-button type="primary" @click="goStart">我要开播</el-button>
    </div>

    <el-tabs v-model="activeTab" class="tabs" @tab-change="handleTabChange">
      <el-tab-pane label="正在直播" name="live">
        <el-empty v-if="!loading && rooms.length === 0" description="暂无直播">
          <el-button type="primary" @click="goStart">去开播</el-button>
        </el-empty>

        <div v-loading="loading" class="room-grid">
          <div
              v-for="room in rooms"
              :key="room.id"
              class="room-card"
              @click="goRoom(room.id)"
          >
            <div class="cover">
              <img v-if="room.coverUrl" :src="room.coverUrl" alt="" />
              <div v-else class="cover-placeholder">
                <el-icon size="36"><VideoCameraFilled /></el-icon>
              </div>
              <span class="live-tag">直播中</span>
              <span class="online">{{ room.onlineCount ?? room.viewCount ?? 0 }} 人</span>
            </div>
            <div class="info">
              <div class="title">{{ room.title }}</div>
              <div class="host">
                <el-avatar :size="24" :src="room.hostAvatar || '/User.jpg'" />
                <span>{{ room.hostNickname || '主播' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="total > size" class="pager">
          <el-pagination
              layout="prev, pager, next"
              :total="total"
              :page-size="size"
              :current-page="page"
              @current-change="(p: number) => { page = p; load() }"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="直播回放" name="replay">
        <el-empty v-if="!replayLoading && replays.length === 0" description="暂无回放" />
        <div v-else v-loading="replayLoading" class="room-grid">
          <div
              v-for="item in replays"
              :key="item.id"
              class="room-card"
              @click="openReplay(item)"
          >
            <div class="cover">
              <img v-if="item.coverUrl" :src="item.coverUrl" alt="" />
              <div v-else class="cover-placeholder">
                <el-icon size="36"><VideoCameraFilled /></el-icon>
              </div>
              <span class="replay-tag">回放</span>
              <span v-if="item.durationSec" class="online">{{ formatDuration(item.durationSec) }}</span>
            </div>
            <div class="info">
              <div class="title">{{ item.title }}</div>
              <div class="host">{{ item.createTime || '' }}</div>
            </div>
          </div>
        </div>

        <div v-if="replayTotal > size" class="pager">
          <el-pagination
              layout="prev, pager, next"
              :total="replayTotal"
              :page-size="size"
              :current-page="replayPage"
              @current-change="(p: number) => { replayPage = p; loadReplays() }"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
        v-model="replayVisible"
        :title="replayCurrent?.title || '直播回放'"
        width="820px"
        destroy-on-close
        @closed="closeReplay"
    >
      <div class="replay-player">
        <video ref="replayVideoEl" controls playsinline class="player"></video>
        <div v-if="replayError" class="player-error">{{ replayError }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.live-square {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
}
.sub {
  margin: 4px 0 0;
  color: var(--ink-secondary, #666);
  font-size: 13px;
}
.tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}
.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  min-height: 120px;
}
.room-card {
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-card, #fff);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.15s ease;
}
.room-card:hover {
  transform: translateY(-3px);
}
.cover {
  position: relative;
  aspect-ratio: 16/9;
  background: #1a1a1a;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}
.live-tag,
.replay-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
.live-tag { background: #fb7299; }
.replay-tag { background: #409eff; }
.online {
  position: absolute;
  right: 8px;
  bottom: 8px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}
.info {
  padding: 12px;
}
.title {
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.host {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-secondary, #666);
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.replay-player {
  position: relative;
  width: 100%;
  aspect-ratio: 16/9;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}
.replay-player .player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.player-error {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ddd;
  font-size: 13px;
  background: rgba(0, 0, 0, 0.45);
  text-align: center;
  padding: 0 16px;
}
</style>

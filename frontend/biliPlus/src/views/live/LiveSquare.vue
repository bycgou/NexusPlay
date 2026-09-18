<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listLiveRooms, type LiveRoom } from '@/api/live'

const router = useRouter()
const rooms = ref<LiveRoom[]>([])
const total = ref(0)
const page = ref(1)
const size = 12
const loading = ref(false)

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

const goRoom = (id: number) => {
  router.push(`/live/room/${id}`)
}

const goStart = () => {
  router.push('/live/start')
}

onMounted(load)
</script>

<template>
  <div class="live-square">
    <div class="page-header">
      <div>
        <h2>直播广场</h2>
        <p class="sub">正在直播 {{ total }} 个房间</p>
      </div>
      <el-button type="primary" @click="goStart">我要开播</el-button>
    </div>

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
  </div>
</template>

<script lang="ts">
import { VideoCameraFilled } from '@element-plus/icons-vue'
export default { components: { VideoCameraFilled } }
</script>

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
.live-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #fb7299;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
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
</style>

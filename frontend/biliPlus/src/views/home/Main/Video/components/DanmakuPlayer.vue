<!-- src/views/home/Main/Video/components/DanmakuPlayer.vue -->
<template>
  <div ref="playerContainer" class="dplayer-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import DPlayer from 'dplayer'
import 'dplayer/dist/DPlayer.min.css'

const props = defineProps({
  src: { type: String, default: '' },
  poster: { type: String, default: '' },
  videoId: { type: [String, Number], default: '' },
})

const playerRef = ref(null)
const playerContainer = ref(null)

const destroyPlayer = () => {
  if (playerRef.value) {
    try {
      playerRef.value.destroy()
    } catch (e) {
      console.warn('销毁播放器失败', e)
    }
    playerRef.value = null
  }
}

/**
 * DPlayer 的 apiBackend 必须挂在根配置 options.apiBackend，
 * 不能放在 danmaku 里（放在 danmaku 里会被忽略，走默认 axios 后端）。
 * 默认后端：GET {api}v3/?id=xx，期望 {code:0,data:[[time,type,color,author,text],...]}
 * 默认发送：POST {api}v3/ 且不带 JWT → 会 401。
 */
const createApiBackend = () => ({
  read({ url, success, error }) {
    fetch(url, { credentials: 'same-origin' })
      .then((res) => {
        if (!res.ok) throw new Error('HTTP ' + res.status)
        return res.json()
      })
      .then((json) => {
        if (json && Number(json.code) === 0 && Array.isArray(json.data)) {
          // 兼容数组协议 [time, type, color, author, text]
          const list = json.data.map((row) => {
            if (Array.isArray(row)) {
              const typeNum = Number(row[1])
              const type =
                typeNum === 5 || typeNum === '5'
                  ? 'top'
                  : typeNum === 4 || typeNum === '4'
                    ? 'bottom'
                    : 'right'
              return {
                time: Number(row[0]) || 0,
                type,
                color: row[2] || '#FFFFFF',
                author: row[3] || 'guest',
                text: row[4] || '',
              }
            }
            return {
              time: Number(row.time) || 0,
              type: row.type || 'right',
              color: row.color || '#FFFFFF',
              author: row.author || 'guest',
              text: row.text || '',
            }
          })
          success(list)
        } else {
          console.warn('弹幕接口返回异常:', json)
          success([])
        }
      })
      .catch((err) => {
        console.warn('弹幕读取失败:', err)
        success([])
      })
  },
  send({ data, success, error }) {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录再发送弹幕')
      error && error('未登录')
      return
    }
    const typeMap = { top: '2', bottom: '3', right: '1' }
    // DPlayer 的 color 可能是 #fff、#ffffff 或数字 16777215
    let color = data.color
    if (typeof color === 'number') {
      color = '#' + color.toString(16).padStart(6, '0')
    } else if (typeof color === 'string' && /^\d+$/.test(color)) {
      color = '#' + Number(color).toString(16).padStart(6, '0')
    } else if (typeof color === 'string' && !color.startsWith('#')) {
      color = '#' + color
    }
    fetch('/api/pp/user/danmakuv3', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        videoId: Number(props.videoId),
        text: data.text,
        time: Math.floor(Number(data.time) || 0),
        color: color || '#ffffff',
        type: typeMap[data.type] || '1',
      }),
    })
      .then(async (res) => {
        const json = await res.json().catch(() => ({}))
        if (res.ok && json.code === 1) {
          ElMessage.success('弹幕发送成功')
          success && success()
        } else {
          const msg = json.msg || `发送失败(${res.status})`
          ElMessage.error(msg)
          error && error(msg)
        }
      })
      .catch((err) => {
        console.error('弹幕发送异常', err)
        ElMessage.error('网络错误，弹幕发送失败')
        error && error(err.message)
      })
  },
})

const initPlayer = async () => {
  if (!props.src || !playerContainer.value) return

  destroyPlayer()
  await nextTick()
  if (!playerContainer.value) return

  try {
    playerRef.value = new DPlayer({
      container: playerContainer.value,
      video: {
        url: props.src,
        poster: props.poster || '',
        type: 'auto',
      },
      // 注意：apiBackend 在根配置
      apiBackend: createApiBackend(),
      danmaku: {
        enable: true,
        id: String(props.videoId || ''),
        // DPlayer 拼接 {api}v3/?id=xx
        api: '/api/pp/user/danmakuv3/',
        user: 'biliplus',
        bottom: '15%',
        unlimited: true,
        speedRate: 0.5,
      },
      autoplay: false,
      controls: true,
      loop: false,
      mutex: true,
    })

    playerRef.value.on('error', () => {
      console.error('DPlayer 播放错误, videoUrl:', props.src)
    })
  } catch (e) {
    console.error('初始化 DPlayer 失败:', e)
  }
}

watch(
  () => [props.src, props.videoId],
  () => {
    if (props.src) initPlayer()
  }
)

onMounted(() => {
  if (props.src) initPlayer()
})

onUnmounted(() => {
  destroyPlayer()
})
</script>

<style scoped>
.dplayer-container {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: #000;
  border-radius: 8px;
  overflow: hidden;
}
</style>

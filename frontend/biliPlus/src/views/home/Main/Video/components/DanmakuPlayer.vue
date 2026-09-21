<!-- src/views/home/Main/Video/components/DanmakuPlayer.vue -->
<template>
  <div ref="playerContainer" class="dplayer-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import DPlayer from 'dplayer'
import 'dplayer/dist/DPlayer.min.css'
import { usePlayerSettings } from '@/composables/usePlayerSettings'
import {
  resolveDanmakuBottom,
  resolveDanmakuOpacity,
  resolveDanmakuSeconds,
  resolveFontSize,
  resolvePlaybackRate,
  resolveVolume
} from '@/constants/playerSettings'

const props = defineProps({
  src: { type: String, default: '' },
  poster: { type: String, default: '' },
  videoId: { type: [String, Number], default: '' },
})

const emit = defineEmits(['ready', 'timeupdate', 'ended'])

const playerRef = ref(null)
const playerContainer = ref(null)

// 与设置页共用同一份播放设置（见 composables/usePlayerSettings.ts）
const { settings } = usePlayerSettings()

/** 弹幕速度需要覆盖 DPlayer 自带的 5s 关键帧，用带 id 的样式表压过它的类选择器 */
let styleEl = null
let styleId = ''

const ensureDanmakuSpeedStyle = () => {
  if (!styleId) {
    styleId = 'np-danmaku-speed-' + Math.random().toString(36).slice(2, 9)
  }
  if (!styleEl || !styleEl.isConnected) {
    styleEl = document.createElement('style')
    styleEl.id = styleId
    document.head.appendChild(styleEl)
  }
  const seconds = resolveDanmakuSeconds(settings.danmakuSpeed)
  styleEl.textContent = `
    #${styleId} .dplayer-danmaku .dplayer-danmaku-right.dplayer-danmaku-move {
      animation-duration: ${seconds}s !important;
    }
  `
}

/**
 * DPlayer 1.25 只把 opacity / unlimited 透传到弹幕实例，
 * 字号、显示区域、速度都需要在挂载后直接作用到 DOM。
 */
const applySettings = () => {
  const player = playerRef.value
  if (!player) return

  // 弹幕开关：保留播放器内的入口，仅控制显隐
  if (player.danmaku) {
    if (settings.danmakuEnabled) {
      player.danmaku.show()
    } else {
      player.danmaku.hide()
    }
    player.danmaku.opacity(resolveDanmakuOpacity(settings.danmakuOpacity))
  }

  const danmakuEl = playerContainer.value?.querySelector('.dplayer-danmaku')
  if (danmakuEl) {
    danmakuEl.style.fontSize = `${resolveFontSize(settings.danmakuFontSize)}px`
    danmakuEl.style.bottom = resolveDanmakuBottom(settings.danmakuArea)
  }

  ensureDanmakuSpeedStyle()

  // 音量 / 倍速作用于 video 元素（DPlayer 的 volume() 会同步控制器 UI）
  try {
    player.volume(resolveVolume(settings.volume))
  } catch (e) {
    console.warn('应用默认音量失败', e)
  }
  if (player.video) {
    player.video.playbackRate = resolvePlaybackRate(settings.playbackRate)
  }
}

const destroyPlayer = () => {
  if (playerRef.value) {
    try {
      playerRef.value.destroy()
    } catch (e) {
      console.warn('销毁播放器失败', e)
    }
    playerRef.value = null
  }
  if (styleEl && styleEl.isConnected) {
    styleEl.remove()
  }
  styleEl = null
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
        unlimited: true,
      },
      // 自动播放交给父组件在 seek 之后显式触发，避免被浏览器策略打断
      autoplay: false,
      volume: resolveVolume(settings.volume),
      controls: true,
      loop: false,
      mutex: true,
    })

    applySettings()

    playerRef.value.on('loadedmetadata', () => {
      applySettings()
      emit('ready', {
        duration: playerRef.value?.video?.duration || 0,
      })
    })

    playerRef.value.on('timeupdate', () => {
      const video = playerRef.value?.video
      if (!video) return
      emit('timeupdate', {
        currentTime: video.currentTime || 0,
        duration: video.duration || 0,
      })
    })

    playerRef.value.on('ended', () => {
      emit('ended')
    })

    playerRef.value.on('error', () => {
      console.error('DPlayer 播放错误, videoUrl:', props.src)
    })
  } catch (e) {
    console.error('初始化 DPlayer 失败:', e)
  }
}

// 播放器就绪后设置页的改动即时生效（字号/区域/速度/透明度/音量/倍速）
watch(
  () => [
    settings.danmakuEnabled,
    settings.danmakuOpacity,
    settings.danmakuFontSize,
    settings.danmakuSpeed,
    settings.danmakuArea,
    settings.volume,
    settings.playbackRate
  ],
  () => {
    if (playerRef.value) applySettings()
  }
)

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

defineExpose({
  seek(seconds) {
    const player = playerRef.value
    if (!player) return
    const duration = player.video?.duration || 0
    const target = Math.max(0, Number(seconds) || 0)
    player.seek(duration > 0 ? Math.min(target, duration) : target)
  },
  play() {
    // 未静音自动播放可能被浏览器拒绝，降级为静音播放而不是静默失败
    playerRef.value?.play()?.catch?.(() => {
      try {
        playerRef.value?.video && (playerRef.value.video.muted = true)
        playerRef.value?.play()
      } catch (e) {
        console.warn('自动播放被浏览器拦截', e)
      }
    })
  },
  pause() {
    playerRef.value?.pause()
  },
  currentTime() {
    return playerRef.value?.video?.currentTime || 0
  },
  duration() {
    return playerRef.value?.video?.duration || 0
  }
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

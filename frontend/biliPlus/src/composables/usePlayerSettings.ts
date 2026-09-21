import { useLocalSettings } from '@/composables/useLocalSettings'
import { PLAYER_SETTINGS_DEFAULTS, type PlayerSettings } from '@/constants/playerSettings'

type PlayerSettingsApi = ReturnType<typeof useLocalSettings<PlayerSettings>>

let cached: PlayerSettingsApi | null = null

/**
 * 播放设置的进程内单例。
 *
 * 设置页、播放器、视频详情都需要读同一份播放设置；如果各自调用 useLocalSettings，
 * 一个视频页会重复拉取同一份服务端设置。这里共享同一个响应式实例，
 * 服务端同步只在首次挂载时发生一次。
 */
export function usePlayerSettings(): PlayerSettingsApi {
  if (!cached) {
    cached = useLocalSettings('player', PLAYER_SETTINGS_DEFAULTS)
  }
  return cached
}

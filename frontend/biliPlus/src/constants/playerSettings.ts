/**
 * 播放器设置的冻结契约。
 *
 * 设置页（PlayerSettings.vue）与播放器（DanmakuPlayer.vue / VideoDetail.vue）共用同一份
 * defaults 与映射函数，避免出现「设置页写的键」与「播放器读的键」不一致。
 * 需要新增/调整字段时只改这里。
 */

export type DanmakuSpeed = 'slow' | 'normal' | 'fast'
export type DanmakuArea = 'top' | 'half' | 'full'

export interface PlayerSettings {
  /** 进入播放页后自动开始播放 */
  autoplay: boolean
  /** 播完自动切推荐下一条 */
  autoNext: boolean
  /** 记忆播放进度 */
  rememberProgress: boolean
  /** 默认音量，设置页口径为百分比 0-100 */
  volume: number
  /** 默认倍速 */
  playbackRate: number
  danmakuEnabled: boolean
  /** 弹幕不透明度，设置页口径为百分比 0-100 */
  danmakuOpacity: number
  danmakuSpeed: DanmakuSpeed
  /** 弹幕显示区域：仅顶部 / 半屏 / 全屏 */
  danmakuArea: DanmakuArea
  /** 弹幕字号，px */
  danmakuFontSize: number
}

export const PLAYER_SETTINGS_DEFAULTS: PlayerSettings = {
  autoplay: false,
  autoNext: true,
  rememberProgress: true,
  volume: 70,
  playbackRate: 1,
  danmakuEnabled: true,
  danmakuOpacity: 80,
  danmakuSpeed: 'normal',
  danmakuArea: 'full',
  danmakuFontSize: 16
}

/** 速度 → 滚动弹幕单趟动画时长（秒），越短越快 */
export const DANMAKU_SCROLL_SECONDS: Record<DanmakuSpeed, number> = {
  slow: 8,
  normal: 5,
  fast: 3
}

/** 显示区域 → 弹幕容器底部预留高度，留白越大可显示行数越少 */
export const DANMAKU_AREA_BOTTOM: Record<DanmakuArea, string> = {
  top: '75%',
  half: '50%',
  full: '0%'
}

export const PLAYBACK_RATE_OPTIONS = [0.5, 0.75, 1, 1.25, 1.5, 2]

const clamp = (value: number, min: number, max: number) =>
  Math.min(max, Math.max(min, value))

/**
 * 换算成 0~1 的比例。
 * 历史上「不透明度/音量」既有 0~1 比例口径（后端早期版本），也有 0~100 百分比口径（当前设置页），
 * 这里在读取持久化数据时统一归一，两种口径都能正确生效。
 */
export const toRatio = (value: unknown, fallback: number): number => {
  const n = Number(value)
  if (!Number.isFinite(n)) return fallback
  return n <= 1 ? clamp(n, 0, 1) : clamp(n / 100, 0, 1)
}

export const resolveVolume = (value: unknown): number =>
  toRatio(value, toRatio(PLAYER_SETTINGS_DEFAULTS.volume, 0.7))

export const resolveDanmakuOpacity = (value: unknown): number =>
  toRatio(value, toRatio(PLAYER_SETTINGS_DEFAULTS.danmakuOpacity, 0.8))

export const resolveFontSize = (value: unknown): number => {
  const n = Number(value)
  if (!Number.isFinite(n) || n <= 0) return PLAYER_SETTINGS_DEFAULTS.danmakuFontSize
  return clamp(Math.round(n), 12, 40)
}

export const resolvePlaybackRate = (value: unknown): number => {
  const n = Number(value)
  if (!Number.isFinite(n) || n <= 0) return PLAYER_SETTINGS_DEFAULTS.playbackRate
  return clamp(n, 0.25, 4)
}

export const resolveDanmakuBottom = (area: unknown): string =>
  DANMAKU_AREA_BOTTOM[(area as DanmakuArea)] ?? DANMAKU_AREA_BOTTOM.full

export const resolveDanmakuSeconds = (speed: unknown): number =>
  DANMAKU_SCROLL_SECONDS[(speed as DanmakuSpeed)] ?? DANMAKU_SCROLL_SECONDS.normal

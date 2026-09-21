/**
 * 视频播放进度记忆（续播）。
 *
 * localStorage 保证未登录也有续播，登录后同一模块内叠加服务端播放历史：
 * - 打开视频：先用本地值即时续播，再拉服务端进度覆盖（服务端是「最后播放位置」的权威）
 * - 播放中：节流写本地，同时上报服务端
 * 调用方（DanmakuPlayer 的宿主页面）不需要感知存储位置。
 */

import { getPlayProgress, reportPlayProgress } from '@/api/playHistory'

const PREFIX = 'nexusplay-progress:'

export interface VideoProgress {
  /** 上次播放位置，秒 */
  time: number
  /** 视频总时长，秒 */
  duration: number
  updatedAt: number
}

const keyOf = (videoId: string | number) => `${PREFIX}${videoId}`

function writeLocal(videoId: string | number, seconds: number, total: number): void {
  try {
    const payload: VideoProgress = { time: seconds, duration: total, updatedAt: Date.now() }
    localStorage.setItem(keyOf(videoId), JSON.stringify(payload))
  } catch {
    // 配额或隐私模式失败时忽略，不影响播放
  }
}

const isLoggedIn = () => {
  try {
    return !!localStorage.getItem('token')
  } catch {
    return false
  }
}

const normalizeId = (videoId: string | number) =>
  videoId === undefined || videoId === null || videoId === '' ? null : videoId

export function readVideoProgress(videoId: string | number): VideoProgress | null {
  const id = normalizeId(videoId)
  if (id === null) return null
  try {
    const raw = localStorage.getItem(keyOf(id))
    if (!raw) return null
    const parsed = JSON.parse(raw) as VideoProgress
    if (!Number.isFinite(Number(parsed?.time))) return null
    return {
      time: Math.max(0, Number(parsed.time)),
      duration: Math.max(0, Number(parsed.duration) || 0),
      updatedAt: Number(parsed.updatedAt) || 0
    }
  } catch {
    return null
  }
}

export function writeVideoProgress(
  videoId: string | number,
  time: number,
  duration: number
): void {
  const id = normalizeId(videoId)
  if (id === null) return

  const seconds = Math.max(0, Math.floor(Number(time) || 0))
  const total = Math.max(0, Math.floor(Number(duration) || 0))
  writeLocal(id, seconds, total)

  if (isLoggedIn()) {
    // 上报失败不应影响播放，服务端仅用于跨设备续播
    reportPlayProgress({
      videoId: Number(id),
      progressSec: seconds,
      durationSec: total
    }).catch(() => { /* 忽略上报失败 */ })
  }
}

export function clearVideoProgress(videoId: string | number): void {
  const id = normalizeId(videoId)
  if (id === null) return
  try {
    localStorage.removeItem(keyOf(id))
  } catch {
    // ignore
  }
}

/** 登录后把服务端进度并入本地缓存，使续播位置以账号为准 */
export async function hydrateProgressFromServer(videoId: string | number): Promise<void> {
  const id = normalizeId(videoId)
  if (id === null || !isLoggedIn()) return
  try {
    const res = await getPlayProgress(id as number | string)
    const remote = res?.data
    if (remote && Number(remote.progressSec) > 0) {
      // 只写本地，避免刚拉下来又上报回服务端
      writeLocal(id, Math.floor(Number(remote.progressSec)), Math.floor(Number(remote.durationSec) || 0))
    }
  } catch {
    // 服务端不可用时继续使用本地进度
  }
}

/** 距结尾不足该秒数视为已看完，不再续播 */
export const PROGRESS_FINISHED_TAIL_SEC = 15

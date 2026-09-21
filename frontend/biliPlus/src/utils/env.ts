/**
 * 前端运行时环境地址。
 *
 * 约定：能用 env 覆盖的一律读 env，读不到就退回「同源」——
 * 部署在反向代理之后无需改代码，也避免把 localhost 写死在源码里。
 */

const raw = (value: unknown): string => (typeof value === 'string' ? value.trim() : '')

/** WebSocket：优先 env，否则用当前站点同源（https 自动升级为 wss） */
export const resolveWsUrl = (path: string, configured?: unknown): string => {
  const explicit = raw(configured)
  if (explicit) return explicit
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${window.location.host}${path}`
}

export const wsChatUrl = () => resolveWsUrl('/ws/chat', import.meta.env.VITE_WS_CHAT)

export const wsLiveUrl = () => resolveWsUrl('/ws/live', import.meta.env.VITE_WS_LIVE)

/** OBS 推流服务器兜底值；未配置时返回空串，由页面提示未配置 */
export const OBS_RTMP_SERVER = raw(import.meta.env.VITE_OBS_RTMP)

/** SRS HTTP-FLV 基地址，与后端 live.srs.http-flv-base 保持一致 */
export const SRS_FLV_BASE = raw(import.meta.env.VITE_SRS_FLV_BASE)

/** 浏览器拉流走前端代理前缀，避免 SRS 跨域 */
export const SRS_FLV_PROXY_PREFIX = raw(import.meta.env.VITE_SRS_FLV_PROXY) || '/srs-live'

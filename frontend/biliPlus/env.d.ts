/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 后端接口基础路径，开发环境走 Vite 代理 */
  readonly VITE_BASE_API?: string
  readonly VITE_API_BASE_URL?: string
  /** 私信 / 通知 WebSocket；留空则使用当前站点同源 */
  readonly VITE_WS_CHAT?: string
  /** 直播房间 WebSocket；留空则使用当前站点同源 */
  readonly VITE_WS_LIVE?: string
  /** OBS 推流服务器（后端 playUrl/pushUrl 不可用时的兜底） */
  readonly VITE_OBS_RTMP?: string
  /** SRS HTTP-FLV 基地址 */
  readonly VITE_SRS_FLV_BASE?: string
  /** 浏览器拉流代理前缀，默认 /srs-live */
  readonly VITE_SRS_FLV_PROXY?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

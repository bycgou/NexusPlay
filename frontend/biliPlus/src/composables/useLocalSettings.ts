import { reactive, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getUserSettings,
  saveUserSettings,
  type UserSettingsPayload
} from '@/api/userSettings'
import { useUserStore } from '@/store/user'

export type SettingsCategory = 'player' | 'quality' | 'notify' | 'privacy' | 'shortcut'

const STORAGE_PREFIX = 'nexusplay-settings:'

const storageKey = (category: SettingsCategory) => `${STORAGE_PREFIX}${category}`

const isLoggedIn = () => {
  try {
    const store = useUserStore()
    return !!(store.isLogin || localStorage.getItem('token'))
  } catch {
    return !!localStorage.getItem('token')
  }
}

const loadLocal = <T extends Record<string, any>>(category: SettingsCategory, defaults: T): T => {
  try {
    const raw = localStorage.getItem(storageKey(category))
    if (!raw) return { ...defaults }
    return { ...defaults, ...JSON.parse(raw) }
  } catch {
    return { ...defaults }
  }
}

/**
 * 用户偏好设置：本地即时生效 + 登录后同步后端
 * - 未登录：仅 localStorage
 * - 已登录：启动时拉取服务端合并，变更防抖写回
 */
export function useLocalSettings<T extends Record<string, any>>(
  category: SettingsCategory,
  defaults: T
) {
  const settings = reactive(loadLocal(category, defaults)) as T
  let ready = false
  let timer: ReturnType<typeof setTimeout> | null = null

  const persistLocal = () => {
    try {
      localStorage.setItem(storageKey(category), JSON.stringify(settings))
    } catch {
      // ignore quota errors
    }
  }

  const persistRemote = async () => {
    if (!isLoggedIn()) return
    try {
      const payload: UserSettingsPayload = {
        [category]: JSON.parse(JSON.stringify(settings))
      }
      await saveUserSettings(payload)
    } catch (e) {
      console.warn(`同步${category}设置失败`, e)
    }
  }

  const schedulePersist = () => {
    persistLocal()
    if (!ready || !isLoggedIn()) return
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      persistRemote()
    }, 400)
  }

  watch(settings, schedulePersist, { deep: true })

  const applyRemote = (remote: Partial<T> | null | undefined) => {
    if (!remote || typeof remote !== 'object') return
    Object.assign(settings, remote)
    persistLocal()
  }

  const loadFromServer = async () => {
    if (!isLoggedIn()) return
    try {
      const res = await getUserSettings()
      if (res.code === 1 && res.data) {
        applyRemote(res.data[category])
      }
    } catch (e) {
      console.warn(`拉取${category}设置失败，继续使用本地`, e)
    }
  }

  const reset = async () => {
    Object.assign(settings, defaults)
    persistLocal()
    if (isLoggedIn()) {
      try {
        // 仅覆盖当前分类为默认值，不影响其它分类
        await persistRemote()
        ElMessage.success('设置已恢复默认并同步到账号')
        return
      } catch (e) {
        console.warn('重置服务端设置失败', e)
      }
    }
    ElMessage.success('设置已恢复默认')
  }

  onMounted(async () => {
    await loadFromServer()
    ready = true
  })

  onBeforeUnmount(() => {
    if (timer) clearTimeout(timer)
  })

  return { settings, reset, persist: persistLocal, reload: loadFromServer }
}

/** 兼容旧调用：完整 localStorage key 形式（仅本地） */
export function useLocalSettingsLegacy<T extends Record<string, any>>(key: string, defaults: T) {
  const load = (): T => {
    try {
      const raw = localStorage.getItem(key)
      if (!raw) return { ...defaults }
      return { ...defaults, ...JSON.parse(raw) }
    } catch {
      return { ...defaults }
    }
  }

  const settings = reactive(load()) as T

  const persist = () => {
    localStorage.setItem(key, JSON.stringify(settings))
  }

  watch(settings, persist, { deep: true })

  const reset = () => {
    Object.assign(settings, defaults)
    persist()
  }

  return { settings, reset, persist }
}

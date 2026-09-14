import { ref, watch } from 'vue'

type Theme = 'light' | 'dark'

const THEME_KEY = 'nexusplay-theme'

// 从 localStorage 或系统偏好初始化
const getInitialTheme = (): Theme => {
  const saved = localStorage.getItem(THEME_KEY) as Theme | null
  if (saved) return saved

  // 检测系统偏好
  if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark'
  }
  return 'light'
}

const theme = ref<Theme>(getInitialTheme())

// 应用主题到 DOM
const applyTheme = (newTheme: Theme) => {
  const html = document.documentElement

  // 添加过渡动画类
  html.classList.add('theme-transition')

  if (newTheme === 'dark') {
    html.setAttribute('data-theme', 'dark')
  } else {
    html.removeAttribute('data-theme')
  }

  // 保存到 localStorage
  localStorage.setItem(THEME_KEY, newTheme)

  // 移除过渡类（防止影响其他动画）
  setTimeout(() => {
    html.classList.remove('theme-transition')
  }, 300)
}

// 初始化时应用
applyTheme(theme.value)

// 监听变化
watch(theme, (newTheme) => {
  applyTheme(newTheme)
})

export function useTheme() {
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  const setTheme = (newTheme: Theme) => {
    theme.value = newTheme
  }

  return {
    theme,
    isDark: () => theme.value === 'dark',
    toggleTheme,
    setTheme
  }
}

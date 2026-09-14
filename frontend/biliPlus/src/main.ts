import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router";
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import {createPinia} from "pinia";
import 'video.js/dist/video-js.css'
import 'dplayer/dist/DPlayer.min.css'
import 'font-awesome/css/font-awesome.min.css'
import '@/assets/styles/global.css'
import '@/assets/styles/animations.css'
// 主题会在 useTheme 模块加载时自动初始化
import '@/composables/useTheme'

const pinia = createPinia()
const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.use(ElementPlus)
app.use(pinia)
app.use(router)

app.mount('#app')

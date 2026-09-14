import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { createPinia } from 'pinia'
// 导入路由
import router from './router'

// 创建vue实例
const app = createApp(App)
app.use( router)
app.use( createPinia())

// 挂载路由实例
app.mount('#app')


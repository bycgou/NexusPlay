import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
    plugins: [
        vue(),
        AutoImport({
            resolvers: [ElementPlusResolver()],
            dts: path.resolve(__dirname, 'src/auto-imports.d.ts'),
            // 补充：自动导入Vue和VueRouter的API（可选，提升开发体验）
            imports: ['vue', 'vue-router', 'pinia']
        }),
        Components({
            resolvers: [ElementPlusResolver()],
            dts: path.resolve(__dirname, 'src/components.d.ts'),
            // 补充：指定组件所在目录（默认包含src，显式配置更清晰）
            dirs: ['src/components']
        }),
    ],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        },
        extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    server: {
        host: '0.0.0.0',
        port: 5174,
        strictPort: true,
        open: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8081',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, '')
            },
            // 局域网访问管理端时同样需要 WS 与媒体代理
            '/ws': {
                target: 'ws://localhost:8081',
                ws: true,
                changeOrigin: true
            },
            '/images': {
                target: 'http://localhost:8081',
                changeOrigin: true
            },
            '/video-files': {
                target: 'http://localhost:8081',
                changeOrigin: true
            },
            // 管理端预览直播走代理，避免 SRS CORS
            // SRS 未监听 IPv6 回环，target 固定用 127.0.0.1
            '/srs-live': {
                target: 'http://127.0.0.1:8080',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/srs-live/, '/live')
            }
        }
    },
    // 确保类型文件被正确处理
    optimizeDeps: {
        exclude: []
    }
})
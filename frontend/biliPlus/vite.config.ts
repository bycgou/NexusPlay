import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// 导入 Element Plus 自动导入插件
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig(({ mode }) => ({
    plugins: [
        vue(),
        // 生产环境不加载 DevTools
        mode === 'development' && vueDevTools(),
        AutoImport({
            resolvers: [ElementPlusResolver()],
            dts: 'src/auto-imports.d.ts',
            imports: ['vue'],
        }),
        Components({
            resolvers: [
                ElementPlusResolver({
                    directives: true,
                    resolveIcons: true,
                }),
            ],
            dts: 'src/components.d.ts',
        }),
    ].filter(Boolean),
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    server: {
        host: '0.0.0.0',
        port: 5173,
        open: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8081',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ''),
            },
            // WebSocket：直播弹幕/PK/连麦信令、私信。不代理的话，局域网访问者会连到
            // Vite 自己（返回 index.html），握手永远失败
            '/ws': {
                target: 'ws://localhost:8081',
                ws: true,
                changeOrigin: true,
            },
            // 媒体资源：后端把 URL 存成相对路径（/images/...、/video-files/...），
            // 由这里转发到本机后端，局域网访问者才能加载封面与视频
            '/images': {
                target: 'http://localhost:8081',
                changeOrigin: true,
            },
            '/video-files': {
                target: 'http://localhost:8081',
                changeOrigin: true,
            },
            // 浏览器拉流走代理，避免 SRS CORS
            // 注意：SRS 在 Docker Desktop + WSL2 下只监听本机回环，
            // 此代理在「你的电脑」上执行，因此对局域网观众同样有效。
            // target 用 127.0.0.1 而不是 localhost：SRS 未监听 IPv6 回环（[::1]:8080 不通），
            // 走 localhost 时 Node 会先试 IPv6 再回退，实测多花约 280ms
            '/srs-live': {
                target: 'http://127.0.0.1:8080',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/srs-live/, '/live'),
            },
        },
    },
    optimizeDeps: {
        include: ['emoji-mart-vue-fast'],
    },
    build: {
        // 代码分割配置
        rollupOptions: {
            output: {
                manualChunks: {
                    // 将大型依赖单独打包
                    'element-plus': ['element-plus'],
                    'vue-vendor': ['vue', 'vue-router', 'pinia'],
                    'video-player': ['video.js', 'dplayer', 'hls.js', 'flv.js'],
                },
            },
        },
        // 启用 CSS 代码分割
        cssCodeSplit: true,
        // 设置 chunk 大小警告阈值
        chunkSizeWarningLimit: 600,
        // 生产环境移除 console
        minify: 'esbuild',
        target: 'es2015',
    },
}))
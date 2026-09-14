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
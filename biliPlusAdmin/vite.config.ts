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
        port: 5174,
        strictPort: true,
        open: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8081',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, '')
            }
        }
    },
    // 确保类型文件被正确处理
    optimizeDeps: {
        exclude: []
    }
})
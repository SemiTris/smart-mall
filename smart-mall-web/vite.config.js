import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 支持 @/xxx 引用 src/xxx
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
  // 注意：这里不需要配置 proxy
  // request.js 的 baseURL 是绝对地址 http://localhost:8080/，
  // 跨域由后端 CorsConfig 处理
})

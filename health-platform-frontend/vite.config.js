import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    //监听所有网卡，保证 http://127.0.0.1:5173 和 http://localhost:5173 都能访问
    //（默认只监听 IPv6 的 ::1，导致 127.0.0.1 连不上；高德白名单只配了 127.0.0.1）
    host: true,
  },
})

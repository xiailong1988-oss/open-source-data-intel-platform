import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  build: {
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }

          if (id.includes('element-plus')) {
            const normalized = id.replace(/\\/g, '/')

            if (normalized.includes('/components/table') || normalized.includes('/components/pagination')) {
              return 'element-table'
            }

            if (
              normalized.includes('/components/form') ||
              normalized.includes('/components/input') ||
              normalized.includes('/components/select') ||
              normalized.includes('/components/date-picker') ||
              normalized.includes('/components/input-number') ||
              normalized.includes('/components/slider') ||
              normalized.includes('/components/radio') ||
              normalized.includes('/components/checkbox')
            ) {
              return 'element-form'
            }

            if (
              normalized.includes('/components/dialog') ||
              normalized.includes('/components/drawer') ||
              normalized.includes('/components/loading') ||
              normalized.includes('/components/message') ||
              normalized.includes('/components/overlay') ||
              normalized.includes('/components/popper') ||
              normalized.includes('/components/tooltip') ||
              normalized.includes('/components/popconfirm')
            ) {
              return 'element-overlay'
            }

            return 'element-core'
          }

          if (id.includes('echarts')) {
            return 'echarts'
          }

          if (id.includes('pinia') || id.includes('vue-router') || id.includes('\\vue\\') || id.includes('/vue/')) {
            return 'vue-vendor'
          }
        },
      },
    },
  },
})

<script setup lang="ts">
/**
 * 首页顶部继续做薄层 chrome，
 * 只保留必要状态和操作，把原本页内大标题彻底收掉，优先把空间还给首屏主内容。
 */
defineProps<{
  district: string
  activeLayer: string
  activeBasemap: string
  latestUpdate: string
  basemapNotice: string
  isSidebarHidden: boolean
}>()

defineEmits<{
  (event: 'focus-district'): void
  (event: 'toggle-sidebar'): void
  (event: 'reset-view'): void
  (event: 'reset-layout'): void
}>()
</script>

<template>
  <header
    class="situation-top-chrome"
    v-motion
    :initial="{ opacity: 0, y: -12 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 520 } }"
  >
    <div class="situation-top-chrome__status">
      <span>{{ district }}</span>
      <span>{{ activeLayer }}</span>
      <span>{{ activeBasemap }}</span>
      <span v-if="basemapNotice" class="is-notice">{{ basemapNotice }}</span>
    </div>

    <div class="situation-top-chrome__actions">
      <button type="button" @click="$emit('reset-layout')">恢复版面</button>
      <button type="button" @click="$emit('toggle-sidebar')">
        {{ isSidebarHidden ? '展开侧栏' : '专注视图' }}
      </button>
      <button type="button" @click="$emit('reset-view')">重置视图</button>
    </div>
  </header>
</template>

<style scoped>
.situation-top-chrome {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  padding: 0;
}

.situation-top-chrome__status {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 6px;
  min-width: 0;
}

.situation-top-chrome__status span {
  display: inline-flex;
  align-items: center;
  min-height: 20px;
  border: 1px solid rgba(123, 165, 223, 0.1);
  padding: 0 7px;
  color: rgba(214, 227, 243, 0.72);
  font-size: 9px;
  background: rgba(6, 14, 23, 0.2);
}

.situation-top-chrome__status .is-notice {
  color: #f7d494;
}

.situation-top-chrome__actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.situation-top-chrome__actions button {
  border: 1px solid rgba(113, 160, 226, 0.14);
  padding: 0 9px;
  min-height: 24px;
  background: rgba(7, 16, 26, 0.2);
  color: #a6c2e3;
  font-size: 10px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.situation-top-chrome__actions button:hover {
  border-color: rgba(120, 175, 247, 0.32);
  background: rgba(17, 32, 50, 0.64);
  color: #f4f8ff;
}

@media (max-width: 1180px) {
  .situation-top-chrome {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .situation-top-chrome__status {
    justify-content: flex-start;
  }

  .situation-top-chrome__actions {
    justify-content: flex-end;
  }
}
</style>

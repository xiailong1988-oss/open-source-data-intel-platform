<script setup lang="ts">
import { Aim, Hide, RefreshRight, View } from '@element-plus/icons-vue'

/**
 * 顶部条只承担终端激活感和极轻状态感，
 * 不再用大标题、厚 pill 群和说明块去压缩地图主画布。
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
}>()
</script>

<template>
  <header class="situation-top-bar">
    <div class="situation-top-bar__brand-group">
      <strong class="situation-top-bar__brand">HAIDIAN TERMINAL</strong>
      <span class="situation-top-bar__active">ACTIVE</span>
    </div>

    <div class="situation-top-bar__status">
      <span class="situation-top-bar__token">{{ district }}</span>
      <span class="situation-top-bar__token">{{ activeLayer }}</span>
      <span class="situation-top-bar__token">{{ activeBasemap }}</span>
      <span class="situation-top-bar__token">更新 {{ latestUpdate }}</span>
      <span v-if="basemapNotice" class="situation-top-bar__token situation-top-bar__token--notice">{{ basemapNotice }}</span>
    </div>

    <div class="situation-top-bar__actions">
      <el-button text size="small" :icon="Aim" @click="$emit('focus-district')">聚焦海淀</el-button>
      <el-button text size="small" :icon="isSidebarHidden ? View : Hide" @click="$emit('toggle-sidebar')">
        {{ isSidebarHidden ? '展开侧栏' : '专注视图' }}
      </el-button>
      <el-button text size="small" :icon="RefreshRight" @click="$emit('reset-view')">重置</el-button>
    </div>
  </header>
</template>

<style scoped>
.situation-top-bar {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  border-bottom: 1px solid rgba(93, 139, 212, 0.1);
  padding: 0 0 8px;
}

.situation-top-bar__brand-group {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.situation-top-bar__brand {
  color: #f1f7ff;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.situation-top-bar__active {
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(255, 101, 101, 0.34);
  border-radius: 4px;
  padding: 3px 8px;
  color: #ff9088;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.situation-top-bar__status {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
  min-width: 0;
}

.situation-top-bar__token {
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(105, 151, 221, 0.1);
  border-radius: 4px;
  padding: 4px 7px;
  background: rgba(8, 17, 29, 0.22);
  color: rgba(221, 234, 248, 0.72);
  font-size: 10px;
  line-height: 1;
  letter-spacing: 0.11em;
  text-transform: uppercase;
}

.situation-top-bar__token--notice {
  color: #f7d78f;
}

.situation-top-bar__actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.situation-top-bar__actions :deep(.el-button) {
  color: #9fb8d6;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.situation-top-bar__actions :deep(.el-button:hover) {
  color: #f3f8ff;
}

@media (max-width: 1440px) {
  .situation-top-bar {
    grid-template-columns: 1fr;
  }

  .situation-top-bar__status {
    justify-content: flex-start;
  }
}
</style>

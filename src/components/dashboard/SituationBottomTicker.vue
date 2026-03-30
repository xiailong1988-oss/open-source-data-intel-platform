<script setup lang="ts">
import { ArrowLeft, ArrowRight, Aim, VideoPause, VideoPlay } from '@element-plus/icons-vue'
import type { DashboardCockpitLayer, DashboardCockpitTickerItem } from '../../types/dashboardCockpit'

/**
 * 底部轮播框是地图主舞台的承接层，
 * 用来把“当前正在发生什么”稳定地交给用户，而不是塞进右侧侧栏。
 */
defineProps<{
  layers: DashboardCockpitLayer[]
  activeLayer: DashboardCockpitLayer
  items: DashboardCockpitTickerItem[]
  activeIndex: number
  isPaused: boolean
}>()

defineEmits<{
  (event: 'layer-select', layer: DashboardCockpitLayer): void
  (event: 'previous'): void
  (event: 'next'): void
  (event: 'toggle-playback'): void
  (event: 'select-item', index: number): void
  (event: 'focus-current'): void
  (event: 'open-detail'): void
  (event: 'open-list'): void
}>()
</script>

<template>
  <section class="situation-ticker">
    <div class="situation-ticker__header">
      <div class="situation-ticker__title">
        <span class="situation-ticker__eyebrow">Playback Axis</span>
        <strong>地图底部态势轮播框</strong>
        <small>自动承接当前专题最重要的事件，始终与地图点位和右侧焦点解释同步。</small>
      </div>
      <div class="situation-ticker__layer-row">
        <button
          v-for="layer in layers"
          :key="layer"
          type="button"
          class="situation-ticker__layer-button"
          :class="{ 'is-active': layer === activeLayer }"
          @click="$emit('layer-select', layer)"
        >
          {{ layer }}
        </button>
      </div>
    </div>

    <div class="situation-ticker__body">
      <div class="situation-ticker__controls">
        <button type="button" class="situation-ticker__icon-button" :disabled="!items.length" @click="$emit('previous')">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <button type="button" class="situation-ticker__icon-button" :disabled="items.length <= 1" @click="$emit('toggle-playback')">
          <el-icon><VideoPlay v-if="isPaused" /><VideoPause v-else /></el-icon>
        </button>
        <button type="button" class="situation-ticker__icon-button" :disabled="!items.length" @click="$emit('next')">
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>

      <div v-if="items.length" class="situation-ticker__active-card">
        <div class="situation-ticker__active-meta">
          <span>{{ items[activeIndex]?.time }}</span>
          <span>{{ items[activeIndex]?.layer }}</span>
          <span>{{ items[activeIndex]?.area }}</span>
          <span>{{ items[activeIndex]?.status }}</span>
        </div>
        <strong>{{ items[activeIndex]?.title }}</strong>
        <p>{{ items[activeIndex]?.summary }}</p>
      </div>
      <div v-else class="situation-ticker__empty">
        <strong>当前图层暂无轮播事件</strong>
        <small>请切换图层或调整筛选条件后重试。</small>
      </div>

      <div class="situation-ticker__actions">
        <button type="button" class="situation-ticker__action-button" :disabled="!items.length" @click="$emit('focus-current')">
          <el-icon><Aim /></el-icon>
          定位地图
        </button>
        <button type="button" class="situation-ticker__action-button" :disabled="!items.length" @click="$emit('open-detail')">查看详情</button>
        <button type="button" class="situation-ticker__action-button" @click="$emit('open-list')">进入列表</button>
      </div>
    </div>

    <div class="situation-ticker__track">
      <button
        v-for="(item, index) in items"
        :key="item.id"
        type="button"
        class="situation-ticker__track-item"
        :class="{ 'is-active': index === activeIndex }"
        @click="$emit('select-item', index)"
      >
        <span class="situation-ticker__track-time">{{ item.time }}</span>
        <span class="situation-ticker__track-type">{{ item.layer }}</span>
        <strong>{{ item.title }}</strong>
        <small>{{ item.area }} · {{ item.summary }}</small>
      </button>
    </div>
  </section>
</template>

<style scoped>
.situation-ticker {
  display: flex;
  flex-direction: column;
  gap: 14px;
  border: 1px solid rgba(118, 170, 242, 0.16);
  border-radius: 24px;
  padding: 16px 18px 18px;
  background: rgba(8, 16, 28, 0.94);
}

.situation-ticker__header,
.situation-ticker__body {
  display: grid;
  gap: 14px;
}

.situation-ticker__header {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
}

.situation-ticker__title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.situation-ticker__eyebrow {
  color: #8ebdff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.situation-ticker__title strong,
.situation-ticker__active-card strong,
.situation-ticker__track-item strong {
  color: var(--platform-text-primary);
}

.situation-ticker__title small,
.situation-ticker__active-card p,
.situation-ticker__track-item small,
.situation-ticker__empty small {
  color: var(--platform-text-secondary);
  line-height: 1.7;
}

.situation-ticker__layer-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.situation-ticker__layer-button,
.situation-ticker__icon-button,
.situation-ticker__action-button,
.situation-ticker__track-item {
  cursor: pointer;
}

.situation-ticker__layer-button,
.situation-ticker__action-button {
  border: 1px solid rgba(118, 170, 242, 0.2);
  border-radius: 999px;
  padding: 8px 12px;
  background: rgba(18, 31, 49, 0.78);
  color: var(--platform-text-secondary);
  font-size: 12px;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.situation-ticker__layer-button:hover,
.situation-ticker__layer-button.is-active,
.situation-ticker__action-button:hover {
  border-color: rgba(118, 170, 242, 0.48);
  background: rgba(34, 63, 96, 0.92);
  color: #f3f8ff;
}

.situation-ticker__body {
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: stretch;
}

.situation-ticker__controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.situation-ticker__icon-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  min-width: 36px;
  border: 1px solid rgba(118, 170, 242, 0.2);
  border-radius: 999px;
  background: rgba(13, 24, 39, 0.86);
  color: #eff5ff;
}

.situation-ticker__icon-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.situation-ticker__active-card,
.situation-ticker__empty {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid rgba(118, 170, 242, 0.14);
  border-radius: 20px;
  padding: 14px 16px;
  background: rgba(13, 24, 39, 0.84);
}

.situation-ticker__active-card p,
.situation-ticker__empty {
  margin: 0;
}

.situation-ticker__active-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: #8ebdff;
  font-size: 11px;
}

.situation-ticker__actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.situation-ticker__track {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(220px, 1fr);
  gap: 10px;
  overflow-x: auto;
}

.situation-ticker__track-item {
  display: flex;
  min-height: 108px;
  flex-direction: column;
  gap: 6px;
  border: 1px solid rgba(118, 170, 242, 0.14);
  border-radius: 18px;
  padding: 12px;
  background: rgba(13, 24, 39, 0.82);
  text-align: left;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.situation-ticker__track-item:hover,
.situation-ticker__track-item.is-active {
  transform: translateY(-1px);
  border-color: rgba(118, 170, 242, 0.4);
  background: rgba(22, 42, 67, 0.92);
}

.situation-ticker__track-time {
  color: #8ebdff;
  font-size: 11px;
  font-weight: 700;
}

.situation-ticker__track-type {
  color: #f8d48e;
  font-size: 11px;
}

@media (max-width: 1440px) {
  .situation-ticker__header,
  .situation-ticker__body {
    grid-template-columns: 1fr;
  }

  .situation-ticker__controls,
  .situation-ticker__actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>

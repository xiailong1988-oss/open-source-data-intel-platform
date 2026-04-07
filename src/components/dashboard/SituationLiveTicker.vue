<script setup lang="ts">
import { computed } from 'vue'
import { VideoPause, VideoPlay } from '@element-plus/icons-vue'
import type { DashboardCockpitFeedFilter, DashboardCockpitLayer, DashboardCockpitTickerItem } from '../../types/dashboardCockpit'

const props = defineProps<{
  items: DashboardCockpitTickerItem[]
  activeItemId: string
  activeFilter: DashboardCockpitFeedFilter
  layers: DashboardCockpitLayer[]
  currentRegionName: string
  mode: 'dynamic' | 'static'
  isPaused: boolean
}>()

const emit = defineEmits<{
  (event: 'select-item', itemId: string): void
  (event: 'change-filter', filter: DashboardCockpitFeedFilter): void
  (event: 'change-mode', mode: 'dynamic' | 'static'): void
  (event: 'toggle-playback'): void
  (event: 'hover-start'): void
  (event: 'hover-end'): void
}>()

const renderItems = computed(() => {
  if (props.mode !== 'dynamic' || props.items.length <= 1) {
    return props.items
  }

  return props.items.concat(props.items)
})

const isAnimated = computed(() => props.mode === 'dynamic' && props.items.length > 1 && !props.isPaused)
</script>

<template>
  <section class="situation-live-ticker">
    <header class="situation-live-ticker__head">
      <div class="situation-live-ticker__title">
        <strong>实时情报快讯</strong>
        <small>{{ currentRegionName }} · {{ items.length }} 条重点动态</small>
      </div>

      <div class="situation-live-ticker__filters">
        <button
          type="button"
          class="situation-live-ticker__filter"
          :class="{ 'is-active': activeFilter === '全部' }"
          @click="emit('change-filter', '全部')"
        >
          全部类型
        </button>
        <button
          v-for="layer in layers"
          :key="layer"
          type="button"
          class="situation-live-ticker__filter"
          :class="{ 'is-active': activeFilter === layer }"
          @click="emit('change-filter', layer)"
        >
          {{ layer }}
        </button>
      </div>

      <div class="situation-live-ticker__actions">
        <button
          type="button"
          class="situation-live-ticker__mode"
          :class="{ 'is-active': mode === 'dynamic' }"
          @click="emit('change-mode', 'dynamic')"
        >
          动态模式
        </button>
        <button
          type="button"
          class="situation-live-ticker__mode"
          :class="{ 'is-active': mode === 'static' }"
          @click="emit('change-mode', 'static')"
        >
          静态模式
        </button>
        <button type="button" class="situation-live-ticker__playback" @click="emit('toggle-playback')">
          <el-icon><VideoPlay v-if="isPaused" /><VideoPause v-else /></el-icon>
          {{ isPaused ? '继续播放' : '暂停播放' }}
        </button>
      </div>
    </header>

    <div
      v-if="items.length"
      class="situation-live-ticker__viewport"
      @mouseenter="emit('hover-start')"
      @mouseleave="emit('hover-end')"
    >
      <div class="situation-live-ticker__track" :class="{ 'is-animated': isAnimated, 'is-static': !isAnimated }">
        <button
          v-for="(item, index) in renderItems"
          :key="`${item.id}-${index}`"
          type="button"
          class="situation-live-ticker__item"
          :class="{ 'is-active': item.id === activeItemId }"
          @click="emit('select-item', item.id)"
        >
          <span>{{ item.time }}｜{{ item.area }}｜{{ item.layer }}</span>
          <strong>{{ item.title }}</strong>
        </button>
      </div>
    </div>

    <div v-else class="situation-live-ticker__empty">
      当前地区暂无快讯，请切换地区或图层查看。
    </div>
  </section>
</template>

<style scoped>
.situation-live-ticker {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.situation-live-ticker__head {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
}

.situation-live-ticker__title {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.situation-live-ticker__title strong {
  color: #eef6ff;
  font-size: 16px;
}

.situation-live-ticker__title small,
.situation-live-ticker__empty {
  color: rgba(205, 220, 240, 0.72);
  font-size: 12px;
}

.situation-live-ticker__filters,
.situation-live-ticker__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.situation-live-ticker__filters {
  overflow-x: auto;
  scrollbar-width: none;
}

.situation-live-ticker__filters::-webkit-scrollbar {
  display: none;
}

.situation-live-ticker__filter,
.situation-live-ticker__mode,
.situation-live-ticker__playback {
  min-height: 28px;
  border: 1px solid rgba(118, 171, 246, 0.1);
  border-radius: 999px;
  padding: 0 12px;
  background: rgba(7, 14, 23, 0.42);
  color: rgba(214, 228, 244, 0.76);
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.22s ease, background 0.22s ease, color 0.22s ease, transform 0.22s ease;
}

.situation-live-ticker__filter:hover,
.situation-live-ticker__mode:hover,
.situation-live-ticker__playback:hover,
.situation-live-ticker__filter.is-active,
.situation-live-ticker__mode.is-active {
  transform: translateY(-1px);
  border-color: rgba(124, 190, 255, 0.34);
  background: rgba(18, 34, 51, 0.84);
  color: #f4f8ff;
}

.situation-live-ticker__playback {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.situation-live-ticker__viewport {
  overflow: hidden;
  border: 1px solid rgba(118, 171, 246, 0.1);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(8, 15, 24, 0.8) 0%, rgba(8, 15, 24, 0.62) 100%);
  padding: 10px 0;
}

.situation-live-ticker__track {
  display: flex;
  align-items: stretch;
  gap: 12px;
  width: max-content;
  padding: 0 14px;
}

.situation-live-ticker__track.is-animated {
  animation: cockpit-ticker-marquee 38s linear infinite;
}

.situation-live-ticker__track.is-static {
  width: 100%;
  overflow-x: auto;
}

.situation-live-ticker__item {
  display: flex;
  min-width: 280px;
  max-width: 360px;
  flex-direction: column;
  gap: 6px;
  border: 1px solid rgba(118, 171, 246, 0.08);
  border-radius: 16px;
  background: rgba(9, 17, 27, 0.72);
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.22s ease, border-color 0.22s ease, box-shadow 0.22s ease, background 0.22s ease;
}

.situation-live-ticker__item span {
  color: rgba(143, 193, 255, 0.78);
  font-size: 11px;
}

.situation-live-ticker__item strong {
  color: #f3f8ff;
  font-size: 14px;
  line-height: 1.45;
}

.situation-live-ticker__item:hover,
.situation-live-ticker__item.is-active {
  transform: translateY(-2px);
  border-color: rgba(125, 194, 255, 0.34);
  background: rgba(17, 33, 49, 0.92);
  box-shadow: 0 18px 34px rgba(3, 10, 18, 0.28);
}

.situation-live-ticker__empty {
  min-height: 58px;
  border: 1px dashed rgba(118, 171, 246, 0.12);
  border-radius: 16px;
  padding: 18px 16px;
}

@keyframes cockpit-ticker-marquee {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(calc(-50% - 6px));
  }
}

@media (max-width: 1280px) {
  .situation-live-ticker__head {
    grid-template-columns: 1fr;
  }

  .situation-live-ticker__actions {
    flex-wrap: wrap;
  }
}
</style>

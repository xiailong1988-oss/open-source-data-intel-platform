<script setup lang="ts">
import type { DashboardCockpitLayer } from '../../types/dashboardCockpit'

interface SignalItem {
  id: string
  label: string
  value: string
  hint: string
}

/**
 * 地图顶部 overlay strip 只允许保留一层。
 * 它承担极轻的态势摘要与图层切换，不再展开成后台式工具带。
 */
defineProps<{
  signalItems: SignalItem[]
  layers: DashboardCockpitLayer[]
  activeLayer: DashboardCockpitLayer
  isHighlightExpanded: boolean
}>()

defineEmits<{
  (event: 'signal-click', signalId: string): void
  (event: 'layer-select', layer: DashboardCockpitLayer): void
  (event: 'toggle-highlights'): void
}>()
</script>

<template>
  <div
    class="situation-map-strip"
    v-motion
    :initial="{ opacity: 0, y: -10 }"
    :enter="{ opacity: 1, y: 0, transition: { delay: 140, duration: 520 } }"
  >
    <div class="situation-map-strip__signals">
      <button
        v-for="item in signalItems"
        :key="item.id"
        type="button"
        class="situation-map-strip__signal"
        :title="item.hint"
        @click="$emit('signal-click', item.id)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </button>
    </div>

    <div class="situation-map-strip__layers">
      <button type="button" class="situation-map-strip__action" @click="$emit('toggle-highlights')">
        {{ isHighlightExpanded ? '收起全部重要信息' : '显示全部重要信息' }}
      </button>
      <button
        v-for="layer in layers"
        :key="layer"
        type="button"
        class="situation-map-strip__layer"
        :class="{ 'is-active': layer === activeLayer }"
        @click="$emit('layer-select', layer)"
      >
        {{ layer }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.situation-map-strip {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  min-height: 46px;
  border: 1px solid rgba(122, 170, 236, 0.1);
  border-radius: 18px;
  padding: 6px 10px;
  background:
    linear-gradient(180deg, rgba(6, 13, 22, 0.42) 0%, rgba(6, 13, 22, 0.18) 100%);
  backdrop-filter: blur(18px);
  box-shadow: inset 0 1px 0 rgba(169, 205, 255, 0.04);
}

.situation-map-strip__signals,
.situation-map-strip__layers {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.situation-map-strip__signals {
  overflow-x: auto;
  scrollbar-width: none;
}

.situation-map-strip__signals::-webkit-scrollbar {
  display: none;
}

.situation-map-strip__signal,
.situation-map-strip__layer,
.situation-map-strip__action {
  border: 1px solid rgba(122, 170, 236, 0.08);
  background: rgba(7, 16, 27, 0.24);
  color: rgba(216, 229, 244, 0.76);
  cursor: pointer;
  transition: border-color 0.24s ease, background 0.24s ease, color 0.24s ease, transform 0.24s ease;
}

.situation-map-strip__signal:hover,
.situation-map-strip__layer:hover,
.situation-map-strip__layer.is-active,
.situation-map-strip__action:hover {
  border-color: rgba(122, 170, 236, 0.34);
  background: rgba(16, 30, 48, 0.6);
  color: #f4f8ff;
  transform: translateY(-1px);
}

.situation-map-strip__signal {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 8px;
  white-space: nowrap;
}

.situation-map-strip__signal span {
  color: rgba(194, 215, 239, 0.78);
  font-size: 9px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.situation-map-strip__signal strong {
  color: #f2f7ff;
  font-size: 12px;
}

.situation-map-strip__layer {
  padding: 5px 9px;
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  white-space: nowrap;
}

.situation-map-strip__action {
  padding: 5px 10px;
  font-size: 10px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  white-space: nowrap;
}

@media (max-width: 1560px) {
  .situation-map-strip {
    grid-template-columns: 1fr;
    align-items: stretch;
  }

  .situation-map-strip__layers {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
}
</style>

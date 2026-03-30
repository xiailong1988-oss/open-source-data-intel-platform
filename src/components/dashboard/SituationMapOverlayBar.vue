<script setup lang="ts">
import type { DashboardCockpitLayer } from '../../types/dashboardCockpit'

interface SignalItem {
  id: string
  label: string
  value: string
  hint: string
}

/**
 * 地图顶部只保留一条窄横条，
 * 让摘要高亮与图层切换融成一层，不再拆出第二层工具带。
 */
defineProps<{
  signalItems: SignalItem[]
  layers: DashboardCockpitLayer[]
  activeLayer: DashboardCockpitLayer
}>()

defineEmits<{
  (event: 'signal-click', signalId: string): void
  (event: 'layer-select', layer: DashboardCockpitLayer): void
}>()
</script>

<template>
  <div class="map-overlay">
    <div class="map-overlay__summary">
      <button
        v-for="item in signalItems"
        :key="item.id"
        type="button"
        class="map-overlay__summary-chip"
        :title="item.hint"
        @click="$emit('signal-click', item.id)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </button>
    </div>

    <div class="map-overlay__layers">
      <button
        v-for="layer in layers"
        :key="layer"
        type="button"
        class="map-overlay__layer-chip"
        :class="{ 'is-active': layer === activeLayer }"
        @click="$emit('layer-select', layer)"
      >
        {{ layer }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.map-overlay {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid rgba(116, 170, 240, 0.12);
  border-radius: 6px;
  padding: 6px 8px;
  background: rgba(5, 12, 22, 0.54);
  backdrop-filter: blur(12px);
}

.map-overlay__summary,
.map-overlay__layers {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.map-overlay__summary-chip,
.map-overlay__layer-chip {
  border: 1px solid rgba(120, 170, 240, 0.14);
  background: rgba(7, 16, 28, 0.52);
  color: rgba(216, 230, 245, 0.76);
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.map-overlay__summary-chip:hover,
.map-overlay__layer-chip:hover,
.map-overlay__layer-chip.is-active {
  border-color: rgba(120, 170, 240, 0.36);
  background: rgba(17, 35, 56, 0.84);
  color: #f3f8ff;
  transform: translateY(-1px);
}

.map-overlay__summary-chip {
  display: inline-flex;
  min-width: 78px;
  align-items: center;
  gap: 8px;
  border-radius: 4px;
  padding: 4px 8px;
}

.map-overlay__summary-chip span {
  color: rgba(196, 216, 238, 0.78);
  font-size: 9px;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.map-overlay__summary-chip strong {
  color: #eff6ff;
  font-size: 12px;
}

.map-overlay__layer-chip {
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 10px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

@media (max-width: 1560px) {
  .map-overlay {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

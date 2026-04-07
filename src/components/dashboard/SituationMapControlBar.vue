<script setup lang="ts">
import type { DashboardCockpitBasemap, DashboardCockpitLayer } from '../../types/dashboardCockpit'

withDefaults(
  defineProps<{
    activeRegionName: string
    layers: DashboardCockpitLayer[]
    activeLayer: DashboardCockpitLayer
    activeBasemap: DashboardCockpitBasemap
    isHighlightExpanded: boolean
    overlay?: boolean
    showMeta?: boolean
  }>(),
  {
    overlay: false,
    showMeta: true,
  },
)

defineEmits<{
  (event: 'select-layer', layer: DashboardCockpitLayer): void
  (event: 'toggle-highlights'): void
  (event: 'reset-view'): void
}>()
</script>

<template>
  <section
    class="situation-map-control"
    :class="{
      'is-overlay': overlay,
      'is-compact': !showMeta,
    }"
  >
    <div v-if="showMeta" class="situation-map-control__meta">
      <span>{{ activeRegionName }}</span>
      <span>{{ activeBasemap }}</span>
      <span>程序化视角</span>
    </div>

    <div class="situation-map-control__layers">
      <button
        v-for="layer in layers"
        :key="layer"
        type="button"
        class="situation-map-control__layer"
        :class="{ 'is-active': layer === activeLayer }"
        @click="$emit('select-layer', layer)"
      >
        {{ layer }}
      </button>
    </div>

    <div class="situation-map-control__actions">
      <button type="button" @click="$emit('toggle-highlights')">
        {{ overlay ? (isHighlightExpanded ? '收起引导' : '展开引导') : isHighlightExpanded ? '收起重点引导' : '展开重点引导' }}
      </button>
      <button type="button" @click="$emit('reset-view')">{{ overlay ? '恢复视角' : '恢复地区视角' }}</button>
    </div>
  </section>
</template>

<style scoped>
.situation-map-control {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  min-width: 0;
  border: 1px solid rgba(117, 174, 247, 0.08);
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(7, 14, 23, 0.42) 0%, rgba(7, 14, 23, 0.22) 100%);
  padding: 6px 8px;
  backdrop-filter: blur(12px);
}

.situation-map-control.is-overlay {
  border-color: rgba(117, 174, 247, 0.08);
  background: linear-gradient(180deg, rgba(7, 14, 23, 0.34) 0%, rgba(7, 14, 23, 0.18) 100%);
  box-shadow: 0 12px 24px rgba(2, 8, 15, 0.14);
}

.situation-map-control.is-compact {
  grid-template-columns: minmax(0, 1fr) auto;
  padding: 5px 6px;
}

.situation-map-control__meta,
.situation-map-control__layers,
.situation-map-control__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.situation-map-control__meta span,
.situation-map-control__layer,
.situation-map-control__actions button {
  min-height: 24px;
  border: 1px solid rgba(117, 174, 247, 0.08);
  border-radius: 999px;
  padding: 0 10px;
  background: rgba(7, 15, 24, 0.18);
  color: rgba(214, 228, 244, 0.78);
  font-size: 11px;
  transition: border-color 0.22s ease, background 0.22s ease, color 0.22s ease, transform 0.22s ease;
}

.situation-map-control.is-overlay .situation-map-control__meta span,
.situation-map-control.is-overlay .situation-map-control__layer,
.situation-map-control.is-overlay .situation-map-control__actions button {
  min-height: 22px;
  border-color: rgba(117, 174, 247, 0.08);
  padding: 0 9px;
  background: rgba(7, 15, 24, 0.14);
  font-size: 10px;
}

.situation-map-control__layers {
  overflow-x: auto;
  scrollbar-width: none;
}

.situation-map-control__layers::-webkit-scrollbar {
  display: none;
}

.situation-map-control__actions {
  justify-content: flex-end;
}

.situation-map-control.is-compact .situation-map-control__actions {
  flex-wrap: wrap;
}

.situation-map-control__layer,
.situation-map-control__actions button {
  cursor: pointer;
}

.situation-map-control__layer:hover,
.situation-map-control__layer.is-active,
.situation-map-control__actions button:hover {
  transform: translateY(-1px);
  border-color: rgba(121, 197, 255, 0.24);
  background: rgba(17, 33, 51, 0.48);
  color: #f4f9ff;
}

@media (max-width: 1180px) {
  .situation-map-control,
  .situation-map-control.is-compact {
    grid-template-columns: 1fr;
  }

  .situation-map-control__meta,
  .situation-map-control__actions {
    flex-wrap: wrap;
  }
}
</style>

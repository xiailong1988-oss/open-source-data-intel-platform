<script setup lang="ts">
import type { DashboardCockpitStreamEntry, DashboardCockpitZone } from '../../types/dashboardCockpit'

defineProps<{
  currentRegionName: string
  recentItems: DashboardCockpitStreamEntry[]
  zones: DashboardCockpitZone[]
}>()

defineEmits<{
  (event: 'select-item', pointId: string): void
  (event: 'select-zone', zoneId: string): void
}>()
</script>

<template>
  <section class="event-list-panel">
    <header class="event-list-panel__head">
      <div>
        <strong>事件与片区观察</strong>
        <small>{{ currentRegionName }} 的优先处置与热点片区</small>
      </div>
    </header>

    <div class="event-list-panel__block">
      <span class="event-list-panel__label">优先事件</span>
      <button
        v-for="item in recentItems.slice(0, 5)"
        :key="item.id"
        type="button"
        class="event-list-panel__item"
        @click="$emit('select-item', item.relatedPointId)"
      >
        <div>
          <strong>{{ item.title }}</strong>
          <small>{{ item.time }} · {{ item.regionName }} · {{ item.status }}</small>
        </div>
        <span>{{ item.eventType }}</span>
      </button>
    </div>

    <div class="event-list-panel__block">
      <span class="event-list-panel__label">重点片区</span>
      <button
        v-for="zone in zones.slice(0, 4)"
        :key="zone.id"
        type="button"
        class="event-list-panel__zone"
        @click="$emit('select-zone', zone.id)"
      >
        <div>
          <strong>{{ zone.name }}</strong>
          <small>{{ zone.emphasis }}</small>
        </div>
        <span>热度 {{ zone.heat }}</span>
      </button>
    </div>
  </section>
</template>

<style scoped>
.event-list-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
  overflow: auto;
  padding-right: 4px;
}

.event-list-panel__head strong,
.event-list-panel__item strong,
.event-list-panel__zone strong {
  color: #eff6ff;
}

.event-list-panel__head small,
.event-list-panel__label,
.event-list-panel__item small,
.event-list-panel__zone small {
  color: rgba(203, 220, 240, 0.72);
  font-size: 12px;
}

.event-list-panel__head strong {
  font-size: 18px;
}

.event-list-panel__block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 0 0 auto;
}

.event-list-panel__label {
  font-size: 11px;
}

.event-list-panel__item,
.event-list-panel__zone {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border: 1px solid rgba(118, 171, 246, 0.08);
  border-radius: 16px;
  background: rgba(8, 16, 27, 0.58);
  padding: 12px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.22s ease, background 0.22s ease, transform 0.22s ease;
}

.event-list-panel__item:hover,
.event-list-panel__zone:hover {
  transform: translateY(-1px);
  border-color: rgba(126, 192, 255, 0.28);
  background: rgba(18, 34, 52, 0.8);
}

.event-list-panel__item div,
.event-list-panel__zone div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 5px;
}

.event-list-panel__item strong,
.event-list-panel__zone strong {
  font-size: 14px;
  line-height: 1.35;
}

.event-list-panel__item span,
.event-list-panel__zone span {
  color: #9fd3ff;
  font-size: 12px;
  white-space: nowrap;
}
</style>

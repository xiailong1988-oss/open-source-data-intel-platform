<script setup lang="ts">
import type { DashboardCockpitLayer, DashboardCockpitRegion } from '../../types/dashboardCockpit'

defineProps<{
  currentRegion: DashboardCockpitRegion
  totalEvents: number
  warningCount: number
  watchCount: number
  hotspotCount: number
  activityIndex: number
  districtShare: number
  layerStats: Array<{
    layer: DashboardCockpitLayer
    count: number
  }>
}>()
</script>

<template>
  <section class="region-summary-panel">
    <header class="region-summary-panel__head">
      <div>
        <strong>{{ currentRegion.name }}</strong>
        <small>{{ currentRegion.description }}</small>
      </div>
      <span class="region-summary-panel__chip">{{ currentRegion.emphasis }}</span>
    </header>

    <div class="region-summary-panel__hero">
      <div>
        <span>当前地区事件总量</span>
        <strong>{{ totalEvents }}</strong>
      </div>
      <div>
        <span>活跃度指数</span>
        <strong>{{ activityIndex }}</strong>
      </div>
    </div>

    <div class="region-summary-panel__metrics">
      <article>
        <span>风险预警</span>
        <strong>{{ warningCount }}</strong>
      </article>
      <article>
        <span>重点关注</span>
        <strong>{{ watchCount }}</strong>
      </article>
      <article>
        <span>热点事件</span>
        <strong>{{ hotspotCount }}</strong>
      </article>
      <article>
        <span>占全区比重</span>
        <strong>{{ districtShare }}%</strong>
      </article>
    </div>

    <div class="region-summary-panel__layers">
      <article v-for="item in layerStats" :key="item.layer">
        <span>{{ item.layer }}</span>
        <strong>{{ item.count }}</strong>
      </article>
    </div>
  </section>
</template>

<style scoped>
.region-summary-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.region-summary-panel__head,
.region-summary-panel__hero,
.region-summary-panel__metrics,
.region-summary-panel__layers {
  display: grid;
  gap: 10px;
}

.region-summary-panel__head {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: flex-start;
}

.region-summary-panel__head strong,
.region-summary-panel__hero strong,
.region-summary-panel__metrics strong,
.region-summary-panel__layers strong {
  color: #eff6ff;
}

.region-summary-panel__head strong {
  font-size: 20px;
}

.region-summary-panel__head small,
.region-summary-panel__hero span,
.region-summary-panel__metrics span,
.region-summary-panel__layers span {
  color: rgba(203, 220, 240, 0.72);
  font-size: 12px;
  line-height: 1.5;
}

.region-summary-panel__chip {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  border: 1px solid rgba(118, 171, 246, 0.14);
  border-radius: 999px;
  padding: 0 12px;
  background: rgba(11, 22, 34, 0.62);
  color: #9fd3ff;
  font-size: 12px;
}

.region-summary-panel__hero {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.region-summary-panel__hero > div,
.region-summary-panel__metrics article,
.region-summary-panel__layers article {
  border: 1px solid rgba(118, 171, 246, 0.08);
  border-radius: 18px;
  background: rgba(9, 17, 27, 0.58);
  padding: 14px;
}

.region-summary-panel__hero strong {
  display: block;
  margin-top: 6px;
  font-size: 32px;
  line-height: 1;
}

.region-summary-panel__metrics {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.region-summary-panel__metrics strong,
.region-summary-panel__layers strong {
  display: block;
  margin-top: 6px;
  font-size: 22px;
  line-height: 1;
}

.region-summary-panel__layers {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}
</style>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import HaidianCockpitPanel from '../../components/dashboard/HaidianCockpitPanel.vue'
import { dashboardCockpitMock } from '../../mock/dashboardCockpit'
import type { DashboardCockpitLayer } from '../../types/dashboardCockpit'

const route = useRoute()
const cockpitOverview = dashboardCockpitMock

const layerQueryMap: Record<string, DashboardCockpitLayer> = {
  风险预警: '风险预警',
  突发事件: '突发事件',
  热点事件: '热点事件',
  重点关注: '重点关注',
  重点事件: '热点事件',
  专题研判: '重点关注',
}

/**
 * 首页仍然兼容旧页面和其他工作台带回来的 query，
 * 但视觉骨架已经改成替换式新首页，因此这里只负责把 query 落到新的首页图层视角。
 */
const initialCockpitLayer = computed<DashboardCockpitLayer>(() => {
  const rawLayer = typeof route.query.layer === 'string' ? route.query.layer : ''
  return layerQueryMap[rawLayer] ?? '风险预警'
})
</script>

<template>
  <section class="dashboard dashboard--crucix platform-page-shell">
    <HaidianCockpitPanel :overview="cockpitOverview" :initial-layer="initialCockpitLayer" />
  </section>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex: 1 1 auto;
  min-width: 0;
  min-height: 0;
}

.dashboard :deep(.haidian-cockpit) {
  flex: 1 1 auto;
  height: calc(100vh - var(--platform-header-height) - 32px);
  min-height: calc(100vh - var(--platform-header-height) - 32px);
}
</style>

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
 * 旧页面和专题工作台仍可能带 query 回流到首页，
 * 这里统一映射到当前海淀驾驶舱图层，避免重构后回到错误视角。
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
  min-width: 0;
}

.dashboard :deep(.haidian-cockpit__frame) {
  min-height: calc(100vh - 124px);
}
</style>

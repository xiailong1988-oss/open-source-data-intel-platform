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
  position: relative;
  display: flex;
  flex: 1 1 auto;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(4, 10, 18, 0.18) 0%, rgba(4, 10, 18, 0.52) 100%),
    url('/backgrounds/haidian-cockpit-tech-bg.svg') center/cover no-repeat;
  box-shadow: inset 0 1px 0 rgba(138, 190, 255, 0.08);
}

.dashboard::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 14%, rgba(103, 178, 255, 0.12), transparent 28%),
    radial-gradient(circle at 84% 22%, rgba(52, 240, 208, 0.1), transparent 24%),
    linear-gradient(180deg, rgba(5, 11, 19, 0.08) 0%, rgba(5, 11, 19, 0.32) 100%);
  pointer-events: none;
}

.dashboard :deep(.haidian-cockpit) {
  position: relative;
  z-index: 1;
  flex: 1 1 auto;
  height: calc(100vh - var(--platform-header-height) - 22px);
  min-height: calc(100vh - var(--platform-header-height) - 22px);
}
</style>

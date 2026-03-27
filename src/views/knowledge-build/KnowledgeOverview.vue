<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { EChartsOption } from '../../lib/echarts'
import ChartCard from '../../components/charts/ChartCard.vue'
import {
  entityTypeDistributionMock,
  hotEventMockList,
  knowledgeMetricsMock,
  relationTypeDistributionMock,
} from '../../mock/knowledgeBuild'

const router = useRouter()
const axisLabelColor = '#9eb3cb'
const subtleGridColor = 'rgba(116, 145, 184, 0.16)'
const legendColor = '#a8bbd3'

const entityTypeOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff', '#3bd2c7', '#6b88ff', '#82d1f8', '#ffbd6f'],
  tooltip: { trigger: 'item' },
  legend: {
    orient: 'vertical',
    right: 8,
    top: 'middle',
    textStyle: { color: legendColor },
  },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['32%', '50%'],
      data: entityTypeDistributionMock.map((item) => ({ name: item.type, value: item.value })),
      label: { show: false },
    },
  ],
}))

const relationTypeOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff'],
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 40, right: 20, top: 20, bottom: 30 },
  xAxis: {
    type: 'category',
    data: relationTypeDistributionMock.map((item) => item.type),
    axisLine: { lineStyle: { color: subtleGridColor } },
    axisLabel: { color: axisLabelColor },
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: subtleGridColor } },
    axisLabel: { color: axisLabelColor },
  },
  series: [
    {
      type: 'bar',
      barWidth: 28,
      data: relationTypeDistributionMock.map((item) => item.value),
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
      },
      label: {
        show: true,
        position: 'top',
      },
    },
  ],
}))

const riskTagMap: Record<string, 'danger' | 'warning' | 'info'> = {
  高: 'danger',
  中: 'warning',
  低: 'info',
}

const metricRouteMap: Record<string, () => void> = {
  entityTotal: () => router.push({ path: '/knowledge-build/entity-management', query: { from: 'overview' } }),
  relationTotal: () => router.push({ path: '/smart-search', query: { keyword: '关系网络', from: 'overview' } }),
  eventTotal: () => router.push({ path: '/knowledge-build/event-management', query: { from: 'overview' } }),
  tagTotal: () => router.push({ path: '/smart-search', query: { keyword: '标签', from: 'overview' } }),
  newEntityToday: () =>
    router.push({ path: '/knowledge-build/entity-management', query: { keyword: '今日', from: 'overview' } }),
  newEventToday: () =>
    router.push({ path: '/knowledge-build/event-management', query: { keyword: '今日', from: 'overview' } }),
}

const handleMetricClick = (metricId: string) => {
  metricRouteMap[metricId]?.()
}

const onEntityTypeClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) {
    return
  }
  router.push({ path: '/knowledge-build/entity-management', query: { entityType: String(payload.name), autoOpen: 'first' } })
}

const onRelationTypeClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) {
    return
  }
  router.push({ path: '/smart-search', query: { keyword: String(payload.name), mode: '语义检索' } })
}

const openHotEvent = (eventName: string) => {
  router.push({ path: '/knowledge-build/event-management', query: { keyword: eventName, autoOpen: 'first' } })
}
</script>

<template>
  <section class="knowledge-overview">
    <el-row :gutter="16">
      <el-col v-for="metric in knowledgeMetricsMock" :key="metric.id" :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="metric-card metric-card--clickable" @click="handleMetricClick(metric.id)">
          <span class="metric-card__label">{{ metric.label }}</span>
          <strong class="metric-card__value">{{ metric.value }}</strong>
          <span class="metric-card__change">{{ metric.change }}</span>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <ChartCard title="实体类型分布图" subtitle="知识实体结构" :option="entityTypeOption" @chart-click="onEntityTypeClick" />
      </el-col>
      <el-col :xs="24" :lg="12">
        <ChartCard title="关系类型分布图" subtitle="实体关系网络" :option="relationTypeOption" @chart-click="onRelationTypeClick" />
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="table-card__header">热点事件列表</div>
      </template>
      <el-table :data="hotEventMockList" stripe>
        <el-table-column label="事件名称" min-width="220">
          <template #default="{ row }">
            <el-button link class="cell-link" @click="openHotEvent(row.eventName)">{{ row.eventName }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="eventType" label="事件类型" width="120" />
        <el-table-column prop="heat" label="热度值" width="100" />
        <el-table-column label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="riskTagMap[row.riskLevel]">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
      </el-table>
    </el-card>
  </section>
</template>

<style scoped>
.knowledge-overview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.knowledge-overview :deep(.el-col) {
  margin-bottom: 16px;
}

.metric-card {
  border: 1px solid var(--platform-border-subtle);
}

.metric-card--clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.metric-card--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 38px rgba(2, 10, 20, 0.34);
}

.metric-card__label {
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.metric-card__value {
  display: block;
  margin-top: 10px;
  color: var(--platform-text-primary);
  font-size: 30px;
  line-height: 1;
}

.metric-card__change {
  display: inline-block;
  margin-top: 10px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.table-card {
  border: 1px solid var(--platform-border-subtle);
}

.table-card__header {
  color: var(--platform-text-primary);
  font-size: 16px;
  font-weight: 600;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}
</style>


<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { EChartsOption } from '../../lib/echarts'
import ChartCard from '../../components/charts/ChartCard.vue'
import {
  abnormalDataMockList,
  governanceMetricsMock,
  governancePipelineMock,
  governanceQualityTrendMock,
} from '../../mock/dataGovernance'

const router = useRouter()
const axisLabelColor = '#9eb3cb'
const subtleGridColor = 'rgba(116, 145, 184, 0.16)'
const legendColor = '#a8bbd3'

const qualityTrendOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff', '#3bd2c7', '#ff8d7d'],
  tooltip: { trigger: 'axis' },
  legend: {
    top: 0,
    textStyle: { color: legendColor },
  },
  grid: { left: 44, right: 20, top: 42, bottom: 26 },
  xAxis: {
    type: 'category',
    data: governanceQualityTrendMock.map((item) => item.date),
    axisLine: { lineStyle: { color: subtleGridColor } },
    axisLabel: { color: axisLabelColor },
  },
  yAxis: [
    {
      type: 'value',
      name: '比率(%)',
      min: 80,
      max: 100,
      splitLine: { lineStyle: { color: subtleGridColor } },
      axisLabel: { color: axisLabelColor },
    },
    {
      type: 'value',
      name: '异常数',
      splitLine: { show: false },
      axisLabel: { color: axisLabelColor },
    },
  ],
  series: [
    {
      name: '质量评分',
      type: 'line',
      smooth: true,
      data: governanceQualityTrendMock.map((item) => item.qualityScore),
    },
    {
      name: '标准化成功率',
      type: 'line',
      smooth: true,
      data: governanceQualityTrendMock.map((item) => item.standardizationRate),
    },
    {
      name: '异常数据量',
      type: 'bar',
      yAxisIndex: 1,
      barWidth: 16,
      data: governanceQualityTrendMock.map((item) => item.abnormalCount),
    },
  ],
}))

const pipelineStatusOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff', '#59c48f', '#ff8d7d'],
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: {
    top: 0,
    textStyle: { color: legendColor },
  },
  grid: { left: 44, right: 20, top: 40, bottom: 26 },
  xAxis: {
    type: 'category',
    data: governancePipelineMock.map((item) => item.stage),
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
      name: '总任务',
      type: 'bar',
      stack: 'total',
      data: governancePipelineMock.map((item) => item.total),
    },
    {
      name: '成功',
      type: 'bar',
      stack: 'success',
      data: governancePipelineMock.map((item) => item.success),
    },
    {
      name: '失败',
      type: 'bar',
      stack: 'success',
      data: governancePipelineMock.map((item) => item.failed),
    },
  ],
}))

const severityTagMap: Record<string, 'danger' | 'warning' | 'info'> = {
  高: 'danger',
  中: 'warning',
  低: 'info',
}

const statusTagMap: Record<string, 'info' | 'warning' | 'success'> = {
  待处理: 'info',
  处理中: 'warning',
  已修复: 'success',
}

const metricRouteMap: Record<string, () => void> = {
  rawData: () => router.push({ path: '/data-governance/standardized-list', query: { from: 'governance' } }),
  cleanedData: () => router.push({ path: '/data-governance/standardized-list', query: { status: '已标准化' } }),
  dedupRate: () => router.push({ path: '/data-governance/standardized-list', query: { keyword: '去重' } }),
  stdRate: () => router.push({ path: '/data-governance/standardized-list', query: { status: '已标准化' } }),
  abnormalData: () => router.push({ path: '/data-governance/standardized-list', query: { status: '待复核' } }),
  todayTasks: () => router.push({ path: '/data-access/task-management', query: { from: 'governance' } }),
}

const handleMetricClick = (metricId: string) => {
  metricRouteMap[metricId]?.()
}

const onQualityTrendClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) {
    return
  }
  router.push({ path: '/data-governance/standardized-list', query: { date: String(payload.name), from: 'quality-trend' } })
}

const onPipelineClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) {
    return
  }
  router.push({ path: '/data-access/task-management', query: { keyword: String(payload.name), from: 'pipeline' } })
}

const openAbnormalRelated = (title: string) => {
  router.push({ path: '/data-governance/standardized-list', query: { keyword: title, autoOpen: 'first' } })
}
</script>

<template>
  <section class="governance-overview">
    <el-row :gutter="16">
      <el-col v-for="metric in governanceMetricsMock" :key="metric.id" :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="metric-card metric-card--clickable" @click="handleMetricClick(metric.id)">
          <span class="metric-card__label">{{ metric.label }}</span>
          <strong class="metric-card__value">{{ metric.value }}</strong>
          <span class="metric-card__compare" :class="metric.trend === 'up' ? 'is-up' : 'is-down'">
            {{ metric.compareText }}
          </span>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <ChartCard
          title="数据质量趋势"
          subtitle="近 7 日治理指标变化"
          :option="qualityTrendOption"
          @chart-click="onQualityTrendClick"
        />
      </el-col>
      <el-col :xs="24" :lg="10">
        <ChartCard
          title="清洗流程状态图"
          subtitle="治理阶段执行情况"
          :option="pipelineStatusOption"
          @chart-click="onPipelineClick"
        />
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="table-card__header">异常数据列表</div>
      </template>
      <el-table :data="abnormalDataMockList" stripe>
        <el-table-column label="异常标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link class="cell-link" @click="openAbnormalRelated(row.title)">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" min-width="180" />
        <el-table-column prop="abnormalType" label="异常类型" width="120" />
        <el-table-column label="严重等级" width="96">
          <template #default="{ row }">
            <el-tag size="small" :type="severityTagMap[row.severity]">{{ row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagMap[row.status]">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discoveredAt" label="发现时间" width="155" />
        <el-table-column prop="owner" label="责任人" width="120" />
      </el-table>
    </el-card>
  </section>
</template>

<style scoped>
.governance-overview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.governance-overview :deep(.el-col) {
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

.metric-card__compare {
  display: inline-block;
  margin-top: 10px;
  font-size: 12px;
}

.metric-card__compare.is-up {
  color: #d36a6a;
}

.metric-card__compare.is-down {
  color: #2f8f63;
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


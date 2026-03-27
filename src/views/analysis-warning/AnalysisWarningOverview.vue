<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { EChartsOption } from '../../lib/echarts'
import ChartCard from '../../components/charts/ChartCard.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import {
  warningMetricsMock,
  warningOverviewRecordsMock,
  warningRiskDistributionMock,
  warningTopicRankingMock,
  warningTrendMock,
} from '../../mock/analysisWarning'

const router = useRouter()
const axisLabelColor = '#9eb3cb'
const subtleGridColor = 'rgba(116, 145, 184, 0.16)'
const legendColor = '#a8bbd3'

const trendOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff', '#ff8d7d', '#59c48f'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, textStyle: { color: legendColor } },
  grid: { left: 40, right: 24, top: 40, bottom: 26 },
  xAxis: {
    type: 'category',
    data: warningTrendMock.map((item) => item.date),
    axisLabel: { color: axisLabelColor },
    axisLine: { lineStyle: { color: subtleGridColor } },
  },
  yAxis: [
    {
      type: 'value',
      axisLabel: { color: axisLabelColor },
      splitLine: { lineStyle: { color: subtleGridColor } },
    },
    {
      type: 'value',
      axisLabel: { color: axisLabelColor },
      splitLine: { show: false },
    },
  ],
  series: [
    { name: '告警总量', type: 'line', smooth: true, data: warningTrendMock.map((item) => item.warningCount) },
    { name: '高风险事件数', type: 'line', smooth: true, data: warningTrendMock.map((item) => item.highRiskCount) },
    {
      name: '异常增长指标',
      type: 'bar',
      yAxisIndex: 1,
      barWidth: 14,
      data: warningTrendMock.map((item) => item.abnormalGrowthIndex),
    },
  ],
}))

const topicRankOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff'],
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 110, right: 20, top: 10, bottom: 20 },
  xAxis: {
    type: 'value',
    axisLabel: { color: axisLabelColor },
    splitLine: { lineStyle: { color: subtleGridColor } },
  },
  yAxis: {
    type: 'category',
    data: warningTopicRankingMock.map((item) => item.topic),
    axisLabel: { color: legendColor },
    axisTick: { show: false },
    axisLine: { show: false },
  },
  series: [
    {
      type: 'bar',
      barWidth: 14,
      data: warningTopicRankingMock.map((item) => item.heat),
      label: { show: true, position: 'right' },
      itemStyle: { borderRadius: [0, 6, 6, 0] },
    },
  ],
}))

const riskDistributionOption = computed<EChartsOption>(() => ({
  color: ['#ff7a82', '#ffbd6f', '#59c48f'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: legendColor } },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '42%'],
      data: warningRiskDistributionMock.map((item) => ({ name: `${item.level}风险`, value: item.value })),
      label: { formatter: '{b}\n{d}%' },
    },
  ],
}))

const levelTagMap: Record<string, 'danger' | 'warning' | 'info'> = {
  高: 'danger',
  中: 'warning',
  低: 'info',
}

const statusTagMap: Record<string, 'warning' | 'info' | 'success'> = {
  待处理: 'info',
  处理中: 'warning',
  已处理: 'success',
}

const hasTrendData = computed(() => warningTrendMock.length > 0)
const hasTopicData = computed(() => warningTopicRankingMock.length > 0)
const hasRiskDistribution = computed(() => warningRiskDistributionMock.length > 0)
const summaryMetricItems = computed(() =>
  warningMetricsMock.map((item) => ({
    ...item,
    hint: item.changeText,
    action: () => handleMetricClick(item.id),
  })),
)

const goToRuleManagement = () => {
  router.push('/analysis-warning/rule-management')
}

const metricRouteMap: Record<string, () => void> = {
  todayWarnings: () => router.push({ path: '/analysis-warning/records', query: { from: 'overview' } }),
  highRiskEvents: () => router.push({ path: '/knowledge-build/event-management', query: { riskLevel: '高' } }),
  hotTopics: () => router.push({ path: '/smart-search', query: { keyword: '热点专题', mode: '语义检索' } }),
  abnormalGrowth: () => router.push({ path: '/smart-search', query: { keyword: '异常增长', mode: '语义检索' } }),
}

const handleMetricClick = (metricId: string) => {
  metricRouteMap[metricId]?.()
}

const onTrendChartClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) {
    return
  }
  router.push({ path: '/analysis-warning/records', query: { date: String(payload.name), from: 'trend' } })
}

const onTopicChartClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) {
    return
  }
  router.push({ path: '/smart-search', query: { keyword: String(payload.name), mode: '语义检索' } })
}

const onRiskChartClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) {
    return
  }
  const level = String(payload.name).replace('风险', '')
  router.push({ path: '/analysis-warning/records', query: { level, from: 'risk' } })
}

const openRecord = (title: string) => {
  router.push({ path: '/analysis-warning/records', query: { keyword: title, autoOpen: 'first' } })
}

const openRule = (ruleName: string) => {
  router.push({ path: '/analysis-warning/rule-management', query: { keyword: ruleName, autoOpen: 'first' } })
}

const openSource = (sourceName: string) => {
  router.push({ path: '/smart-search', query: { keyword: sourceName } })
}
</script>

<template>
  <section class="warning-overview stage3-page">
    <WorkbenchHero
      eyebrow="Alert Workbench"
      title="风险发现与预警研判概览"
      description="聚焦趋势变化、热点主题和处理状态，支持从发现到处置的闭环跟踪。"
    >
      <template #meta>
        <el-button type="primary" plain @click="goToRuleManagement">查看规则配置</el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid :items="summaryMetricItems" />

    <div class="warning-overview__charts-grid">
      <div class="warning-overview__chart-item">
        <ChartCard
          title="趋势分析图"
          subtitle="预警量与异常增长变化"
          :option="trendOption"
          :is-empty="!hasTrendData"
          empty-text="暂无趋势数据"
          @chart-click="onTrendChartClick"
        />
      </div>
      <div class="warning-overview__chart-item">
        <ChartCard
          title="热点主题排行"
          subtitle="高热主题优先处置"
          :option="topicRankOption"
          :is-empty="!hasTopicData"
          empty-text="暂无主题排行数据"
          @chart-click="onTopicChartClick"
        />
      </div>
      <div class="warning-overview__chart-item">
        <ChartCard
          title="风险等级分布"
          subtitle="风险结构占比"
          :option="riskDistributionOption"
          :is-empty="!hasRiskDistribution"
          empty-text="暂无风险分布数据"
          @chart-click="onRiskChartClick"
        />
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="table-card__header">最新预警记录</div>
      </template>
      <el-table :data="warningOverviewRecordsMock" stripe>
        <el-table-column label="预警标题" min-width="230" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link class="cell-link" @click="openRecord(row.title)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="告警等级" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="levelTagMap[row.level]">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源对象" min-width="150">
          <template #default="{ row }">
            <el-button link class="cell-link" @click="openSource(row.sourceObject)">{{ row.sourceObject }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="触发规则" min-width="150">
          <template #default="{ row }">
            <el-button link class="cell-link" @click="openRule(row.triggerRule)">{{ row.triggerRule }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagMap[row.status]">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无预警记录" :image-size="72" />
        </template>
      </el-table>
    </el-card>
  </section>
</template>

<style scoped>
.warning-overview {
  min-width: 0;
}

.warning-overview__metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--platform-card-gap);
}

.warning-overview__charts-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 1fr) minmax(320px, 1fr);
  gap: var(--platform-section-gap);
}

.overview-brief-card,
.metric-card,
.table-card {
  border: 1px solid var(--platform-border-subtle);
}

.overview-brief {
  gap: 16px;
}

.overview-brief h3 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.overview-brief p {
  margin: 8px 0 0;
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.metric-card__label {
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.metric-card--clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.metric-card--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 38px rgba(2, 10, 20, 0.34);
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

.table-card__header {
  color: var(--platform-text-primary);
  font-size: 16px;
  font-weight: 600;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

@media (max-width: 1800px) {
  .warning-overview__metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .warning-overview__charts-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .warning-overview__charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .overview-brief {
    grid-template-columns: 1fr;
  }

  .warning-overview__metrics-grid {
    grid-template-columns: 1fr;
  }
}
</style>


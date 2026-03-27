<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { EChartsOption } from '../../lib/echarts'
import ChartCard from '../../components/charts/ChartCard.vue'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import DashboardQuickWorkbench from '../../components/dashboard/DashboardQuickWorkbench.vue'
import DashboardSituationDigest from '../../components/dashboard/DashboardSituationDigest.vue'
import HaidianCockpitPanel from '../../components/dashboard/HaidianCockpitPanel.vue'
import { dashboardMockData } from '../../mock/dashboard'
import { dashboardCockpitMock } from '../../mock/dashboardCockpit'
import type { DashboardCockpitLayer } from '../../types/dashboardCockpit'
import type { ReportStatus, TaskStatus } from '../../types/dashboard'

const route = useRoute()
const router = useRouter()

const metrics = dashboardMockData.metrics
const sourceDistribution = dashboardMockData.sourceDistribution
const trendData = dashboardMockData.growthTrend
const topicRanking = dashboardMockData.topicRanking
const dataTypeDistribution = dashboardMockData.dataTypeDistribution
const recentTasks = dashboardMockData.recentTasks
const latestReports = dashboardMockData.latestReports
const cockpitOverview = dashboardCockpitMock

const axisLabelColor = '#9eb3cb'
const subtleGridColor = 'rgba(116, 145, 184, 0.16)'
const legendColor = '#a8bbd3'

const layerQueryMap: Record<string, DashboardCockpitLayer> = {
  风险预警: '风险预警',
  突发事件: '突发事件',
  热点事件: '热点事件',
  重点关注: '重点关注',
  重点事件: '热点事件',
  专题研判: '重点关注',
}

/**
 * 旧页面和专题工作台还会带旧 query 回流到首页，
 * 这里统一把旧层名映射到新的海淀驾驶舱图层，避免返回首页后落到错误视角。
 */
const initialCockpitLayer = computed<DashboardCockpitLayer>(() => {
  const rawLayer = typeof route.query.layer === 'string' ? route.query.layer : ''
  return layerQueryMap[rawLayer] ?? '风险预警'
})

const summaryCards = computed(() =>
  metrics.map((item) => ({
    ...item,
    formattedValue: new Intl.NumberFormat('zh-CN').format(item.value),
  })),
)

const digestSummaryItems = computed(() => [
  {
    id: 'urgent-risk',
    label: '高风险线索',
    value: `${cockpitOverview.points.filter((item) => item.riskLevel === '高').length} 项`,
    hint: '海淀区当前需要优先处置或持续盯防的高风险对象。',
  },
  {
    id: 'burst-events',
    label: '突发处置',
    value: `${cockpitOverview.points.filter((item) => item.layer === '突发事件').length} 起`,
    hint: '论坛安保、高校活动和园区演练进入当前值守视线。',
  },
  {
    id: 'hot-spots',
    label: '热点片区',
    value: `${cockpitOverview.zones.filter((item) => item.heat >= 85).length} 片`,
    hint: '中关村、上地、学院路和西北旺是当前热点主片区。',
  },
  {
    id: 'focus-watch',
    label: '重点盯防',
    value: `${cockpitOverview.points.filter((item) => item.layer === '重点关注').length} 项`,
    hint: '围绕重点机构、企业和项目的连续观察对象。',
  },
])

const digestAgendaItems = computed(() => [
  {
    id: 'risk',
    title: cockpitOverview.points.find((item) => item.layer === '风险预警')?.title ?? '暂无重点风险',
    meta: '优先处置队列 · 海淀区',
    actionLabel: '进入预警',
  },
  {
    id: 'event',
    title: cockpitOverview.points.find((item) => item.layer === '突发事件')?.title ?? '暂无突发事件',
    meta: '现场联动 · 海淀区',
    actionLabel: '进入事件',
  },
  {
    id: 'topic',
    title: cockpitOverview.topics[0]?.title ?? '暂无热点主题',
    meta: '驾驶舱专题 · 海淀区',
    actionLabel: '进入检索',
  },
])

const digestKeyRegions = computed(() => cockpitOverview.zones.slice(0, 4).map((item) => item.name))

const sourceDistributionOption = computed<EChartsOption>(() => ({
  color: ['#4d91f2', '#56b8c4', '#82d1f8', '#6b88ff', '#f4b36b'],
  tooltip: { trigger: 'item' },
  legend: {
    bottom: 0,
    icon: 'circle',
    itemHeight: 8,
    textStyle: { color: legendColor, fontSize: 12 },
  },
  series: [
    {
      type: 'pie',
      radius: ['35%', '67%'],
      center: ['50%', '42%'],
      data: sourceDistribution.map((item) => ({ name: item.name, value: item.value })),
      label: { formatter: '{b}\n{d}%' },
      labelLine: { length: 12, length2: 8 },
      emphasis: { scale: true },
    },
  ],
}))

const trendOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff', '#3bd2c7', '#ffbd6f'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, textStyle: { color: legendColor, fontSize: 12 } },
  grid: { left: 46, right: 20, top: 42, bottom: 30 },
  xAxis: {
    type: 'category',
    data: trendData.map((item) => item.date),
    axisLine: { lineStyle: { color: subtleGridColor } },
    axisLabel: { color: axisLabelColor },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: subtleGridColor } },
    axisLabel: { color: axisLabelColor },
  },
  series: [
    { name: '累计数据量', type: 'line', smooth: true, data: trendData.map((item) => item.total) },
    { name: '已治理数据量', type: 'line', smooth: true, data: trendData.map((item) => item.governed) },
    { name: '日增量', type: 'line', smooth: true, data: trendData.map((item) => item.dailyIncrease) },
  ],
}))

const topicRankOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff'],
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 98, right: 24, top: 20, bottom: 20 },
  xAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: subtleGridColor } },
    axisLabel: { color: axisLabelColor },
  },
  yAxis: {
    type: 'category',
    data: topicRanking.map((item) => item.topic),
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: legendColor },
  },
  series: [
    {
      type: 'bar',
      barWidth: 14,
      data: topicRanking.map((item) => item.heat),
      itemStyle: { borderRadius: [0, 8, 8, 0] },
      label: { show: true, position: 'right', color: legendColor },
    },
  ],
}))

const dataTypeOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff', '#56b8c4', '#6b88ff', '#3bd2c7', '#ffbd6f'],
  tooltip: { trigger: 'item' },
  legend: {
    orient: 'vertical',
    top: 'middle',
    right: 8,
    textStyle: { color: legendColor, fontSize: 12 },
  },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['32%', '50%'],
      avoidLabelOverlap: false,
      data: dataTypeDistribution.map((item) => ({ name: item.name, value: item.value })),
      label: { show: false },
    },
  ],
}))

const taskStatusTagMap: Record<TaskStatus, 'success' | 'warning' | 'info' | 'danger'> = {
  运行中: 'warning',
  已完成: 'success',
  待执行: 'info',
  失败: 'danger',
}

const reportStatusTagMap: Record<ReportStatus, 'success' | 'warning' | 'info'> = {
  已发布: 'success',
  审核中: 'warning',
  草稿: 'info',
}

const sourceTypeMap: Record<string, string> = {
  市级政务平台: '政务系统',
  行业业务系统: '业务系统',
  互联网公开信息: '互联网公开数据',
  专题共享平台: '第三方平台',
  物联感知设备: '物联网设备',
}

const openMetricTarget = (metricId: string) => {
  const metricRouteMap: Record<string, () => void> = {
    dataSourceTotal: () => router.push({ path: '/data-access/source-management', query: { from: 'dashboard' } }),
    newDataToday: () => router.push({ path: '/data-governance/standardized-list', query: { from: 'dashboard', timePreset: 'today' } }),
    governedData: () => router.push({ path: '/data-governance/standardized-list', query: { from: 'dashboard', status: '已标准化' } }),
    entityCount: () => router.push({ path: '/knowledge-build/entity-management', query: { from: 'dashboard' } }),
    alertCount: () => router.push({ path: '/analysis-warning/records', query: { from: 'dashboard' } }),
    reportCount: () => router.push({ path: '/report-center', query: { from: 'dashboard' } }),
  }

  metricRouteMap[metricId]?.()
}

const handleDigestSummaryClick = (summaryId: string) => {
  if (summaryId === 'urgent-risk') {
    router.push({ path: '/analysis-warning/records', query: { keyword: '海淀区', level: '高', from: 'dashboard-digest' } })
    return
  }
  if (summaryId === 'burst-events') {
    router.push({ path: '/knowledge-build/event-management', query: { keyword: '海淀区', from: 'dashboard-digest' } })
    return
  }
  if (summaryId === 'hot-spots') {
    router.push({ path: '/smart-search', query: { keyword: '海淀区热点片区', region: '海淀区', mode: '语义检索', from: 'dashboard-digest' } })
    return
  }
  router.push({ path: '/knowledge-build/entity-management', query: { keyword: '海淀区', from: 'dashboard-digest' } })
}

const handleDigestAgendaClick = (agendaId: string) => {
  if (agendaId === 'risk') {
    router.push({ path: '/analysis-warning/records', query: { keyword: cockpitOverview.points.find((item) => item.layer === '风险预警')?.title ?? '海淀区', autoOpen: 'first' } })
    return
  }
  if (agendaId === 'event') {
    router.push({ path: '/knowledge-build/event-management', query: { keyword: cockpitOverview.points.find((item) => item.layer === '突发事件')?.title ?? '海淀区', autoOpen: 'first' } })
    return
  }
  router.push({ path: '/smart-search', query: { keyword: cockpitOverview.topics[0]?.keyword ?? '海淀区热点事件', region: '海淀区', mode: '语义检索' } })
}

const handleDigestRegionClick = (region: string) => {
  router.push({ path: '/smart-search', query: { region: '海淀区', keyword: region, mode: '语义检索', from: 'dashboard-digest' } })
}

const onSourceChartClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) return
  const sourceType = sourceTypeMap[payload.name] ?? '全部'
  router.push({ path: '/data-access/source-management', query: { sourceType, from: 'dashboard-chart' } })
}

const onTrendChartClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) return
  router.push({ path: '/data-governance/standardized-list', query: { date: String(payload.name), from: 'dashboard-chart' } })
}

const onTopicChartClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) return
  router.push({ path: '/smart-search', query: { keyword: String(payload.name), mode: '语义检索', from: 'dashboard-chart' } })
}

const onDataTypeChartClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) return
  router.push({ path: '/smart-search', query: { dataType: String(payload.name), from: 'dashboard-chart' } })
}

const openTaskManagement = (taskName: string) => {
  router.push({ path: '/data-access/task-management', query: { keyword: taskName, autoOpenLog: 'first' } })
}

const openReportDetail = (reportId: string, reportName: string) => {
  router.push({ path: `/report-center/detail/${reportId}`, query: { reportName } })
}

const getTaskStatusTagType = (status: TaskStatus) => taskStatusTagMap[status]
const getReportStatusTagType = (status: ReportStatus) => reportStatusTagMap[status]
</script>

<template>
  <section class="dashboard platform-page-shell">
    <HaidianCockpitPanel :overview="cockpitOverview" :initial-layer="initialCockpitLayer" />

    <div class="dashboard__summary-strip">
      <button v-for="item in summaryCards" :key="item.id" type="button" class="dashboard__summary-card" @click="openMetricTarget(item.id)">
        <span>{{ item.label }}</span>
        <strong>{{ item.formattedValue }}{{ item.unit }}</strong>
        <small>{{ item.description }}</small>
      </button>
    </div>

    <div class="dashboard__operations-grid">
      <section class="dashboard__section">
        <DashboardSituationDigest
          :summary-items="digestSummaryItems"
          :agenda-items="digestAgendaItems"
          :key-regions="digestKeyRegions"
          @summary-click="handleDigestSummaryClick"
          @agenda-click="handleDigestAgendaClick"
          @region-click="handleDigestRegionClick"
        />
      </section>

      <section class="dashboard__section">
        <PageSectionHeader
          title="工作台快捷入口"
          description="让检索、问答、预警、报告和专题继续承接首页驾驶舱之后的动作。"
          eyebrow="Workbench"
        />
        <DashboardQuickWorkbench :quick-links="cockpitOverview.quickLinks" :recent-focuses="cockpitOverview.recentFocuses" />
      </section>
    </div>

    <div class="dashboard__lower-grid">
      <section class="dashboard__section">
        <PageSectionHeader
          title="辅助分析图谱"
          description="趋势、来源和结构分布退到第二层，作为驾驶舱后的辅助分析视角。"
          eyebrow="Analysis"
        />
        <div class="dashboard__analysis-grid">
          <ChartCard title="数据增长趋势" subtitle="近 7 日变化" :option="trendOption" @chart-click="onTrendChartClick" />
          <ChartCard title="热点主题排行" subtitle="主题热度值" :option="topicRankOption" @chart-click="onTopicChartClick" />
          <ChartCard title="数据来源分布" subtitle="按接入渠道统计" :option="sourceDistributionOption" @chart-click="onSourceChartClick" />
          <ChartCard title="数据类型分布" subtitle="结构化与非结构化占比" :option="dataTypeOption" @chart-click="onDataTypeChartClick" />
        </div>
      </section>

      <section class="dashboard__section">
        <PageSectionHeader
          title="继续追踪"
          description="承接驾驶舱之后的任务动作，让任务和报告留在第二层，不再与地图争主位。"
          eyebrow="Tracking"
        />
        <div class="dashboard__tracking-grid">
          <el-card shadow="never" class="dashboard-card">
            <template #header>
              <div class="dashboard-card__header"><span>最近采集任务</span></div>
            </template>
            <el-table :data="recentTasks" size="small">
              <el-table-column label="任务名称" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">
                  <el-button link class="cell-link" @click="openTaskManagement(row.taskName)">{{ row.taskName }}</el-button>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="92">
                <template #default="{ row }">
                  <el-tag size="small" :type="getTaskStatusTagType(row.status as TaskStatus)">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="lastRunAt" label="最近执行" width="144" />
            </el-table>
          </el-card>

          <el-card shadow="never" class="dashboard-card">
            <template #header>
              <div class="dashboard-card__header"><span>最近生成报告</span></div>
            </template>
            <el-table :data="latestReports" size="small">
              <el-table-column label="报告名称" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  <el-button link class="cell-link" @click="openReportDetail(row.id, row.name)">{{ row.name }}</el-button>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="88" />
              <el-table-column label="状态" width="92">
                <template #default="{ row }">
                  <el-tag size="small" :type="getReportStatusTagType(row.status as ReportStatus)">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="generatedAt" label="生成时间" width="144" />
            </el-table>
          </el-card>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.dashboard {
  min-width: 0;
}

.dashboard__summary-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.dashboard__summary-card {
  border: 1px solid rgba(116, 146, 184, 0.16);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(14, 25, 39, 0.92) 0%, rgba(8, 16, 26, 0.96) 100%);
  padding: 12px 14px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.dashboard__summary-card:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  box-shadow: 0 18px 36px rgba(2, 10, 20, 0.26);
}

.dashboard__summary-card span,
.dashboard__summary-card small {
  display: block;
}

.dashboard__summary-card span {
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.dashboard__summary-card strong {
  display: block;
  margin-top: 4px;
  color: var(--platform-text-primary);
  font-size: 20px;
}

.dashboard__summary-card small {
  margin-top: 6px;
  color: var(--platform-text-secondary);
  font-size: 12px;
  line-height: 1.55;
}

.dashboard__operations-grid,
.dashboard__lower-grid,
.dashboard__analysis-grid,
.dashboard__tracking-grid {
  display: grid;
  gap: var(--platform-section-gap);
}

.dashboard__operations-grid {
  grid-template-columns: minmax(420px, 0.98fr) minmax(0, 1.12fr);
}

.dashboard__lower-grid {
  grid-template-columns: minmax(0, 1.16fr) minmax(420px, 0.94fr);
  align-items: start;
}

.dashboard__analysis-grid,
.dashboard__tracking-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.dashboard__section {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dashboard-card {
  border: 1px solid var(--platform-border-subtle);
}

.dashboard-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--platform-text-primary);
  font-size: 15px;
  font-weight: 600;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

@media (max-width: 2200px) {
  .dashboard__summary-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1800px) {
  .dashboard__operations-grid,
  .dashboard__lower-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1440px) {
  .dashboard__analysis-grid,
  .dashboard__tracking-grid,
  .dashboard__summary-strip {
    grid-template-columns: 1fr;
  }
}
</style>

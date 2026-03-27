<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { EChartsOption } from '../../lib/echarts'
import ChartCard from '../../components/charts/ChartCard.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import ViewStatePanel from '../../components/common/ViewStatePanel.vue'
import { getSourceHealthList, getSourceRefreshLogs } from '../../services'
import type { SourceHealthItem, SourceRefreshLogItem } from '../../types/sourceCenter'

const router = useRouter()
const loading = ref(true)
const loadError = ref('')
const healthList = ref<SourceHealthItem[]>([])
const refreshLogs = ref<SourceRefreshLogItem[]>([])
const selectedHealthId = ref('')

const axisLabelColor = '#9eb3cb'
const subtleGridColor = 'rgba(116, 145, 184, 0.16)'

const loadHealthData = async () => {
  loading.value = true
  loadError.value = ''

  try {
    const [healthItems, logItems] = await Promise.all([getSourceHealthList(), getSourceRefreshLogs()])
    healthList.value = healthItems
    refreshLogs.value = logItems
    if (!selectedHealthId.value && healthItems.length) {
      selectedHealthId.value = healthItems[0].id
    }
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '数据健康信息加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadHealthData()
})

const selectedHealth = computed(
  () => healthList.value.find((item) => item.id === selectedHealthId.value) ?? healthList.value[0] ?? null,
)

const delayedList = computed(() => healthList.value.filter((item) => item.status === '延迟'))
const failedList = computed(() => healthList.value.filter((item) => item.status === '异常'))

const metrics = computed(() => [
  {
    id: 'healthy',
    label: '健康信源',
    value: healthList.value.filter((item) => item.status === '健康').length,
    hint: '可稳定支撑首页、专题与检索的信源数量',
    action: () => {
      selectedHealthId.value = healthList.value.find((item) => item.status === '健康')?.id ?? ''
    },
  },
  {
    id: 'delayed',
    label: '延迟信源',
    value: delayedList.value.length,
    hint: '刷新延迟较大的信源，建议联动采集任务查看',
    action: () => {
      selectedHealthId.value = delayedList.value[0]?.id ?? ''
    },
  },
  {
    id: 'failed',
    label: '异常信源',
    value: failedList.value.length,
    hint: '存在失败或部分成功记录，建议优先处置',
    action: () => {
      selectedHealthId.value = failedList.value[0]?.id ?? ''
    },
  },
  {
    id: 'freshness',
    label: '平均新鲜度',
    value: `${Math.round(healthList.value.reduce((sum, item) => sum + item.freshnessMinutes, 0) / Math.max(healthList.value.length, 1))} 分钟`,
    hint: '用于快速观察全局刷新时效是否稳定',
    clickable: false,
  },
])

const distributionOption = computed<EChartsOption>(() => ({
  color: ['#59c48f', '#f0b15f', '#df655d'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#a8bbd3' } },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '42%'],
      data: [
        { name: '健康', value: healthList.value.filter((item) => item.status === '健康').length },
        { name: '延迟', value: delayedList.value.length },
        { name: '异常', value: failedList.value.length },
      ],
      label: { formatter: '{b}\n{d}%' },
    },
  ],
}))

const freshnessOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff'],
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 36, right: 18, top: 24, bottom: 26 },
  xAxis: {
    type: 'category',
    data: healthList.value.map((item) => item.sourceName),
    axisLabel: { color: axisLabelColor, interval: 0, rotate: 18 },
    axisLine: { lineStyle: { color: subtleGridColor } },
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: axisLabelColor },
    splitLine: { lineStyle: { color: subtleGridColor } },
  },
  series: [
    {
      type: 'bar',
      barWidth: 18,
      data: healthList.value.map((item) => item.freshnessMinutes),
      itemStyle: { borderRadius: [8, 8, 0, 0] },
    },
  ],
}))

const openTaskManagement = () => {
  router.push('/data-access/task-management')
}
</script>

<template>
  <section class="data-health stage3-page">
    <WorkbenchHero
      eyebrow="Data Health"
      title="数据健康工作台"
      description="持续观察各信源的更新状态、延迟情况、失败记录与最近刷新链路。"
    >
      <template #meta>
        <el-button @click="router.push('/data-access/source-intelligence')">查看信源列表</el-button>
        <el-button type="primary" @click="openTaskManagement">进入采集任务</el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid v-if="!loading && !loadError" :items="metrics" />

    <ViewStatePanel v-if="loading" state="loading" title="正在加载数据健康信息" description="正在同步信源状态、刷新日志与新鲜度指标。" />
    <ViewStatePanel v-else-if="loadError" state="error" :description="loadError" @retry="loadHealthData" />
    <template v-else>
      <div class="data-health__chart-grid">
        <ChartCard title="健康状态分布" subtitle="信源健康 / 延迟 / 异常占比" :option="distributionOption" :is-empty="!healthList.length" empty-text="暂无健康数据" />
        <ChartCard title="新鲜度对比" subtitle="按信源统计刷新延迟（分钟）" :option="freshnessOption" :is-empty="!healthList.length" empty-text="暂无新鲜度数据" />
      </div>

      <div class="data-health__layout">
        <el-card shadow="never" class="health-panel">
          <template #header>
            <PageSectionHeader title="信源健康列表" description="点击行可查看对应的延迟、失败和刷新计划。" compact />
          </template>
          <el-table :data="healthList" stripe @row-click="selectedHealthId = $event.id">
            <el-table-column label="信源名称" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click.stop="selectedHealthId = row.id">{{ row.sourceName }}</el-button>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '健康' ? 'success' : row.status === '延迟' ? 'warning' : 'danger'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="freshnessMinutes" label="延迟(分钟)" width="110" />
            <el-table-column prop="successRate" label="成功率" width="100">
              <template #default="{ row }">{{ row.successRate }}%</template>
            </el-table-column>
            <el-table-column prop="lastRefreshAt" label="最近刷新" width="170" />
            <template #empty>
              <el-empty description="暂无健康状态数据" :image-size="72" />
            </template>
          </el-table>
        </el-card>

        <div class="health-side">
          <el-card shadow="never" class="health-panel">
            <template #header>
              <PageSectionHeader title="当前焦点" description="承接当前信源的健康摘要与处置建议。" compact />
            </template>
            <ViewStatePanel v-if="!selectedHealth" state="empty" compact title="暂无焦点信源" description="请选择左侧列表中的一项。" />
            <template v-else>
              <div class="health-focus">
                <strong>{{ selectedHealth.sourceName }}</strong>
                <div class="health-focus__meta">
                  <span>状态：{{ selectedHealth.status }}</span>
                  <span>延迟：{{ selectedHealth.freshnessMinutes }} 分钟</span>
                  <span>成功率：{{ selectedHealth.successRate }}%</span>
                </div>
                <p>{{ selectedHealth.issueSummary }}</p>
              </div>
              <div class="health-focus__block">
                <span class="health-focus__label">刷新计划</span>
                <p>最近刷新：{{ selectedHealth.lastRefreshAt }}</p>
                <p>下次计划：{{ selectedHealth.nextRefreshAt }}</p>
              </div>
            </template>
          </el-card>

          <el-card shadow="never" class="health-panel">
            <template #header>
              <PageSectionHeader title="延迟与异常列表" description="优先展示需要值守处理的信源对象。" compact />
            </template>
            <div class="health-chip-list">
              <button v-for="item in [...failedList, ...delayedList]" :key="item.id" type="button" class="health-chip" @click="selectedHealthId = item.id">
                <strong>{{ item.sourceName }}</strong>
                <span>{{ item.status }} · {{ item.freshnessMinutes }} 分钟</span>
              </button>
            </div>
          </el-card>

          <el-card shadow="never" class="health-panel">
            <template #header>
              <PageSectionHeader title="最近刷新记录" description="用于快速判断刷新链路是否正常执行。" compact />
            </template>
            <div class="refresh-log-list">
              <div v-for="item in refreshLogs" :key="item.id" class="refresh-log-item">
                <strong>{{ item.sourceName }}</strong>
                <span>{{ item.time }} · {{ item.action }} · {{ item.result }}</span>
                <small>{{ item.duration }} · {{ item.operator }}</small>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.data-health {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.data-health__chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--platform-section-gap);
}

.data-health__layout {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(360px, 1fr);
  gap: var(--platform-section-gap);
}

.health-side {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.health-panel {
  border: 1px solid var(--platform-border-subtle);
}

.health-focus strong,
.health-chip strong,
.refresh-log-item strong {
  color: var(--platform-text-primary);
}

.health-focus__meta,
.health-chip span,
.refresh-log-item span,
.refresh-log-item small {
  display: block;
  margin-top: 6px;
  color: var(--platform-text-tertiary);
}

.health-focus p,
.health-focus__block p {
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.health-focus__block {
  margin-top: 14px;
}

.health-focus__label {
  color: var(--platform-text-primary);
  font-size: 13px;
  font-weight: 600;
}

.health-chip-list,
.refresh-log-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.health-chip,
.refresh-log-item {
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: var(--platform-card-bg-muted);
  padding: 14px;
  text-align: left;
}

.health-chip {
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.health-chip:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  box-shadow: 0 18px 38px rgba(2, 10, 20, 0.24);
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

@media (max-width: 1400px) {
  .data-health__chart-grid,
  .data-health__layout {
    grid-template-columns: 1fr;
  }
}
</style>

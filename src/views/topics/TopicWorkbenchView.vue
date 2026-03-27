<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { EChartsOption } from '../../lib/echarts'
import ChartCard from '../../components/charts/ChartCard.vue'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import ViewStatePanel from '../../components/common/ViewStatePanel.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import SpatialOverviewPanel from '../../components/dashboard/SpatialOverviewPanel.vue'
import { useAppStore } from '../../stores/app'
import { getTopicWorkbench } from '../../services'
import type { TopicWorkbenchCode, TopicWorkbenchData } from '../../types/topicWorkbench'

const props = defineProps<{
  topicCode: TopicWorkbenchCode
}>()

const router = useRouter()
const appStore = useAppStore()
const loading = ref(true)
const loadError = ref('')
const workbenchData = ref<TopicWorkbenchData | null>(null)

const axisLabelColor = '#9eb3cb'
const subtleGridColor = 'rgba(116, 145, 184, 0.16)'

/**
 * 专题工作台在进入时同步刷新顶部专题上下文，
 * 让页面、顶部统一入口和后续智能问答/检索跳转处于同一语义视角。
 */
const loadTopicWorkbench = async () => {
  loading.value = true
  loadError.value = ''

  try {
    workbenchData.value = await getTopicWorkbench(props.topicCode)
    appStore.setActiveTopic(props.topicCode)
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '专题工作台数据加载失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.topicCode,
  () => {
    void loadTopicWorkbench()
  },
)

onMounted(() => {
  void loadTopicWorkbench()
})

const metricItems = computed(() =>
  (workbenchData.value?.metrics ?? []).map((item) => ({
    ...item,
    action: () => {
      if (item.id === 'briefings') {
        router.push({ path: '/report-center/briefings', query: { topic: workbenchData.value?.briefingKeyword ?? '' } })
        return
      }
      router.push({ path: '/smart-search', query: { topic: props.topicCode, keyword: item.label, mode: '语义检索' } })
    },
  })),
)

const signalOption = computed<EChartsOption>(() => ({
  color: props.topicCode === 'economy' ? ['#f0b15f'] : ['#64a9ff'],
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 100, right: 20, top: 14, bottom: 20 },
  xAxis: {
    type: 'value',
    axisLabel: { color: axisLabelColor },
    splitLine: { lineStyle: { color: subtleGridColor } },
  },
  yAxis: {
    type: 'category',
    data: (workbenchData.value?.signalData ?? []).map((item) => item.label),
    axisLabel: { color: '#a8bbd3' },
    axisTick: { show: false },
    axisLine: { show: false },
  },
  series: [
    {
      type: 'bar',
      barWidth: 16,
      data: (workbenchData.value?.signalData ?? []).map((item) => item.value),
      label: { show: true, position: 'right' },
      itemStyle: { borderRadius: [0, 8, 8, 0] },
    },
  ],
}))

const handleSignalClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name) {
    return
  }

  router.push({ path: '/smart-search', query: { topic: props.topicCode, region: String(payload.name), mode: '语义检索' } })
}

const openQuestion = (question: string) => {
  router.push({
    path: '/smart-qa',
    query: {
      topic: props.topicCode,
      question,
      autoAsk: 'true',
      from: 'topic-workbench',
    },
  })
}

const openBriefing = () => {
  if (!workbenchData.value) {
    return
  }

  router.push({ path: '/report-center/briefings', query: { topic: workbenchData.value.briefingKeyword, autoOpen: 'first' } })
}

const openEvent = (keyword: string) => {
  router.push({ path: '/knowledge-build/event-management', query: { keyword, autoOpen: 'first' } })
}

const openDashboard = () => {
  router.push('/dashboard')
}

const openSearch = () => {
  router.push({ path: '/smart-search', query: { topic: props.topicCode, keyword: workbenchData.value?.briefingKeyword ?? '', mode: '语义检索' } })
}

const openRelatedReport = (reportId?: string, title?: string) => {
  if (reportId) {
    router.push(`/report-center/detail/${reportId}`)
    return
  }

  if (title) {
    router.push({ path: '/report-center/briefings', query: { keyword: title } })
    return
  }

  ElMessage.info('当前简报引用暂未关联正式报告')
}
</script>

<template>
  <section class="topic-workbench stage3-page">
    <WorkbenchHero
      :eyebrow="workbenchData?.eyebrow ?? 'Topic Workbench'"
      :title="workbenchData?.title ?? '专题工作台'"
      :description="workbenchData?.description ?? '正在加载专题工作台。'"
    >
      <template #meta>
        <el-button @click="openDashboard">返回综合态势</el-button>
        <el-button @click="openSearch">进入专题检索</el-button>
        <el-button type="primary" @click="openBriefing">查看专题简报</el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid v-if="!loading && !loadError && workbenchData" :items="metricItems" />

    <ViewStatePanel
      v-if="loading"
      state="loading"
      title="正在加载专题工作台"
      description="正在同步专题 GIS 视图、事件主线和简报引用。"
    />
    <ViewStatePanel v-else-if="loadError" state="error" :description="loadError" @retry="loadTopicWorkbench" />

    <template v-else-if="workbenchData">
      <el-alert type="info" :closable="false" show-icon class="topic-workbench__alert">
        {{ workbenchData.heroHint }}
      </el-alert>

      <SpatialOverviewPanel
        :overview="workbenchData.overview"
        :eyebrow="workbenchData.eyebrow"
        :title="`${workbenchData.title} GIS 主视图`"
        :description="workbenchData.description"
      />

      <div class="topic-workbench__support-grid">
        <ChartCard
          :title="workbenchData.signalTitle"
          :subtitle="workbenchData.signalSubtitle"
          :option="signalOption"
          :is-empty="!workbenchData.signalData.length"
          empty-text="暂无区域热度数据"
          @chart-click="handleSignalClick"
        />

        <el-card shadow="never" class="topic-panel">
          <template #header>
            <PageSectionHeader title="重点事件主线" description="围绕本专题的主事件链继续追踪。" compact />
          </template>
          <div class="topic-list">
            <button v-for="item in workbenchData.focusEvents" :key="item.id" type="button" class="topic-list__item" @click="openEvent(item.keyword)">
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.region }} · {{ item.time }} · {{ item.status }}</span>
                <small>{{ item.summary }}</small>
              </div>
              <el-tag :type="item.level === '高' ? 'danger' : item.level === '中' ? 'warning' : 'info'" size="small">{{ item.level }}</el-tag>
            </button>
          </div>
        </el-card>

        <div class="topic-side-stack">
          <el-card shadow="never" class="topic-panel">
            <template #header>
              <PageSectionHeader title="推荐问题" description="继续向智能问答追问本专题重点问题。" compact />
            </template>
            <div class="question-list">
              <button v-for="item in workbenchData.recommendedQuestions" :key="item.id" type="button" class="question-item" @click="openQuestion(item.question)">
                <strong>{{ item.question }}</strong>
                <span>{{ item.description }}</span>
              </button>
            </div>
          </el-card>

          <el-card shadow="never" class="topic-panel">
            <template #header>
              <PageSectionHeader title="专题简报引用" description="从专题视角直接进入简报与正式报告链路。" compact />
            </template>
            <div class="briefing-list">
              <button v-for="item in workbenchData.briefingReferences" :key="item.id" type="button" class="briefing-item" @click="openRelatedReport(item.reportId, item.title)">
                <strong>{{ item.title }}</strong>
                <span>{{ item.type }} · {{ item.generatedAt }} · {{ item.status }}</span>
                <small>{{ item.summary }}</small>
              </button>
            </div>
          </el-card>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.topic-workbench {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.topic-workbench__alert {
  border: 1px solid rgba(118, 151, 186, 0.16);
}

.topic-workbench__support-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr) minmax(360px, 0.9fr);
  gap: var(--platform-section-gap);
}

.topic-side-stack {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.topic-panel {
  border: 1px solid var(--platform-border-subtle);
}

.topic-list,
.question-list,
.briefing-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.topic-list__item,
.question-item,
.briefing-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: var(--platform-card-bg-muted);
  padding: 14px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.question-item,
.briefing-item {
  flex-direction: column;
}

.topic-list__item:hover,
.question-item:hover,
.briefing-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  box-shadow: 0 18px 38px rgba(2, 10, 20, 0.24);
}

.topic-list__item strong,
.question-item strong,
.briefing-item strong {
  color: var(--platform-text-primary);
}

.topic-list__item span,
.topic-list__item small,
.question-item span,
.briefing-item span,
.briefing-item small {
  display: block;
  margin-top: 6px;
  color: var(--platform-text-tertiary);
  line-height: 1.7;
}

@media (max-width: 1800px) {
  .topic-workbench__support-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .topic-side-stack {
    grid-column: 1 / -1;
  }
}

@media (max-width: 1200px) {
  .topic-workbench__support-grid {
    grid-template-columns: 1fr;
  }
}
</style>

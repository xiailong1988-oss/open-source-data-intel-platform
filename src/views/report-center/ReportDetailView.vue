<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { EChartsOption } from '../../lib/echarts'
import ChartCard from '../../components/charts/ChartCard.vue'
import ViewStatePanel from '../../components/common/ViewStatePanel.vue'
import { getReportDetail, getReportList } from '../../services'
import type { ReportDetail } from '../../types/reportCenter'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const loadError = ref('')
const exporting = ref(false)
const reportDetail = ref<ReportDetail | null>(null)
const axisLabelColor = '#9eb3cb'
const subtleGridColor = 'rgba(116, 145, 184, 0.16)'

/**
 * 报告详情页允许从首页、简报中心和报告列表携带 reportName 等参数进入，
 * 当 mock 中没有对应详情时，用 fallbackDetail 兜住演示链路，避免详情页空白。
 */
const fallbackDetail = async (id: string): Promise<ReportDetail> => {
  const reportName = String(route.query.reportName ?? '')
  const reportList = await getReportList()
  const base = reportList.find((item) => item.id === id) ?? reportList[0]

  return {
    id,
    title: reportName || base?.name || 'AI 辅助分析报告',
    type: base?.type || '专题报告',
    generatedAt: base?.generatedAt || '2026-03-16 09:30',
    creator: base?.creator || '综合研判组',
    version: 'v1.0',
    summary: '当前报告正在生成详细内容，以下为 mock 预览摘要。',
    coreFindings: ['发现1：核心指标存在阶段性波动', '发现2：风险事件需持续跟踪', '发现3：建议联动多部门处置'],
    trendData: [
      { date: '03-10', value: 65 },
      { date: '03-11', value: 68 },
      { date: '03-12', value: 70 },
      { date: '03-13', value: 72 },
      { date: '03-14', value: 74 },
      { date: '03-15', value: 77 },
      { date: '03-16', value: 80 },
    ],
    keyEvents: [{ eventName: '核心事件跟踪中', level: '中', impact: '影响指标稳定性', status: '处理中' }],
    riskJudgment: '综合风险为中等级，需保持持续跟踪。',
    suggestions: ['建议每日复盘关键指标', '建议增强联动处置机制'],
    relatedEntities: ['综合研判中心'],
    referencedContents: ['综合态势看板快照'],
  }
}

const loadReportDetail = async () => {
  loading.value = true
  loadError.value = ''

  try {
    const reportId = String(route.params.id ?? '')
    reportDetail.value = (await getReportDetail(reportId)) ?? (await fallbackDetail(reportId))
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '报告详情加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadReportDetail()
})

const trendOption = computed<EChartsOption>(() => ({
  color: ['#64a9ff'],
  tooltip: { trigger: 'axis' },
  grid: { left: 36, right: 20, top: 30, bottom: 26 },
  xAxis: {
    type: 'category',
    data: reportDetail.value?.trendData.map((item) => item.date) ?? [],
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
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.18 },
      data: reportDetail.value?.trendData.map((item) => item.value) ?? [],
    },
  ],
}))

const levelTagMap: Record<string, 'danger' | 'warning' | 'info'> = {
  高: 'danger',
  中: 'warning',
  低: 'info',
}

const goBack = () => {
  router.push('/report-center/reports')
}

const exportReport = async () => {
  exporting.value = true
  await new Promise((resolve) => setTimeout(resolve, 700))
  exporting.value = false
  ElMessage.success('已导出报告（mock）')
}

const toEvent = (eventName: string) => {
  router.push({ path: '/knowledge-build/event-management', query: { keyword: eventName, autoOpen: 'first' } })
}

const toEntity = (entityName: string) => {
  router.push({ path: '/knowledge-build/entity-management', query: { keyword: entityName, autoOpen: 'first' } })
}

const toContent = (title: string) => {
  router.push({ path: '/data-governance/standardized-list', query: { keyword: title, autoOpen: 'first' } })
}

const toSearch = (keyword: string) => {
  router.push({ path: '/smart-search', query: { keyword, mode: '语义检索' } })
}

/**
 * 趋势图点击后不直接停留在报告页，而是回溯到检索页继续分析，
 * 让报告页具备“可继续追踪来源”的能力，而不是静态终点页面。
 */
const onTrendClick = (params: unknown) => {
  const payload = params as { name?: string }
  if (!payload?.name || !reportDetail.value) {
    return
  }
  toSearch(`${reportDetail.value.title} ${String(payload.name)}`)
}
</script>

<template>
  <section class="report-detail stage3-page">
    <ViewStatePanel
      v-if="loading"
      state="loading"
      title="正在加载报告详情"
      description="正在同步正文摘要、趋势图和关联对象。"
    />
    <ViewStatePanel v-else-if="loadError" state="error" :description="loadError" @retry="loadReportDetail" />
    <template v-else-if="reportDetail">
      <el-card shadow="never" class="report-header-card">
        <div class="report-header">
          <div>
            <h1>{{ reportDetail.title }}</h1>
            <p>AI 辅助分析报告预览</p>
          </div>
          <div class="report-header__actions">
            <el-button @click="goBack">返回列表</el-button>
            <el-button type="primary" :loading="exporting" @click="exportReport">导出报告</el-button>
          </div>
        </div>
        <el-descriptions :column="4" border class="report-meta">
          <el-descriptions-item label="报告编号">{{ reportDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="报告类型">{{ reportDetail.type }}</el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ reportDetail.generatedAt }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ reportDetail.creator }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ reportDetail.version }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="report-section-card">
        <template #header><strong>摘要</strong></template>
        <p class="report-text">{{ reportDetail.summary }}</p>
      </el-card>

      <el-card shadow="never" class="report-section-card">
        <template #header><strong>核心发现</strong></template>
        <el-empty v-if="!reportDetail.coreFindings.length" description="暂无核心发现" :image-size="72" />
        <ol v-else class="ordered-list">
          <li v-for="(item, index) in reportDetail.coreFindings" :key="`${index}-${item}`">{{ item }}</li>
        </ol>
      </el-card>

      <ChartCard
        title="趋势分析图"
        subtitle="关键趋势指标变化（支持点击回溯检索）"
        :option="trendOption"
        :is-empty="!reportDetail.trendData.length"
        empty-text="暂无趋势数据"
        height="300px"
        @chart-click="onTrendClick"
      />

      <el-card shadow="never" class="report-section-card">
        <template #header><strong>重点事件列表</strong></template>
        <el-table :data="reportDetail.keyEvents" stripe>
          <el-table-column label="事件名称" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <el-button link class="cell-link" @click="toEvent(row.eventName)">{{ row.eventName }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="levelTagMap[row.level]">{{ row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="impact" label="影响判断" min-width="220" />
          <el-table-column prop="status" label="状态" width="120" />
          <template #empty>
            <el-empty description="暂无重点事件" :image-size="72" />
          </template>
        </el-table>
      </el-card>

      <el-card shadow="never" class="report-section-card">
        <template #header><strong>关联对象</strong></template>
        <div class="link-row">
          <span>相关实体：</span>
          <el-empty v-if="!(reportDetail.relatedEntities?.length ?? 0)" description="暂无关联实体" :image-size="56" />
          <template v-else>
            <el-tag
              v-for="entity in reportDetail.relatedEntities"
              :key="entity"
              type="info"
              class="clickable-tag"
              @click="toEntity(entity)"
            >
              {{ entity }}
            </el-tag>
          </template>
        </div>
        <div class="link-row">
          <span>引用内容：</span>
          <el-empty v-if="!(reportDetail.referencedContents?.length ?? 0)" description="暂无引用内容" :image-size="56" />
          <template v-else>
            <el-button
              v-for="content in reportDetail.referencedContents"
              :key="content"
              link
              class="cell-link"
              @click="toContent(content)"
            >
              {{ content }}
            </el-button>
          </template>
        </div>
      </el-card>

      <el-card shadow="never" class="report-section-card">
        <template #header><strong>风险判断</strong></template>
        <el-alert type="warning" :closable="false" show-icon>{{ reportDetail.riskJudgment }}</el-alert>
      </el-card>

      <el-card shadow="never" class="report-section-card">
        <template #header><strong>建议措施</strong></template>
        <el-empty v-if="!reportDetail.suggestions.length" description="暂无建议措施" :image-size="72" />
        <ol v-else class="ordered-list">
          <li v-for="(item, index) in reportDetail.suggestions" :key="`${index}-${item}`">{{ item }}</li>
        </ol>
      </el-card>
    </template>
  </section>
</template>

<style scoped>
.report-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-header-card,
.report-section-card {
  border: 1px solid var(--platform-border-subtle);
}

.report-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.report-header h1 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 28px;
}

.report-header p {
  margin: 8px 0 0;
  color: var(--platform-text-secondary);
}

.report-header__actions {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.report-meta {
  margin-top: 14px;
}

.report-text {
  margin: 0;
  color: var(--platform-text-secondary);
  line-height: 1.9;
}

.ordered-list {
  margin: 0;
  padding-left: 18px;
  color: var(--platform-text-secondary);
  line-height: 1.9;
}

.link-row {
  margin-bottom: 12px;
}

.link-row > span {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.clickable-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

@media (max-width: 900px) {
  .report-header {
    flex-direction: column;
  }
}
</style>

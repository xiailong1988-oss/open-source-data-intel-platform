<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import ViewStatePanel from '../../components/common/ViewStatePanel.vue'
import type { BriefingListItem, BriefingStatus, BriefingTemplateItem, BriefingType } from '../../types/briefingCenter'
import { getBriefingList, getBriefingTemplates } from '../../services'

const route = useRoute()
const router = useRouter()

type BriefingTypeFilter = BriefingType | '全部'
type BriefingStatusFilter = BriefingStatus | '全部'

const loading = ref(true)
const loadError = ref('')
const briefingList = ref<BriefingListItem[]>([])
const templateList = ref<BriefingTemplateItem[]>([])
const selectedBriefingId = ref('')
const dialogVisible = ref(false)
const generating = ref(false)
const exportingId = ref('')

const filterForm = reactive({
  keyword: '',
  type: '全部' as BriefingTypeFilter,
  status: '全部' as BriefingStatusFilter,
  topic: '全部',
})

const createForm = reactive({
  title: '',
  type: '专题简报' as BriefingType,
  topic: '经济专题',
  region: '北京市',
  keyword: '',
})

const typeOptions: BriefingTypeFilter[] = ['全部', '每日态势简报', '专题简报', '区域简报', '事件专报']
const statusOptions: BriefingStatusFilter[] = ['全部', '已生成', '生成中', '待审核']
const topicOptions = computed(() => ['全部', ...Array.from(new Set(briefingList.value.map((item) => item.topic)))])

const loadBriefingData = async () => {
  loading.value = true
  loadError.value = ''

  try {
    const [briefings, templates] = await Promise.all([getBriefingList(), getBriefingTemplates()])
    briefingList.value = briefings
    templateList.value = templates
    if (!selectedBriefingId.value && briefings.length) {
      selectedBriefingId.value = briefings[0].id
    }
    applyRouteQuery()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '简报中心数据加载失败'
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() =>
  briefingList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword ||
      item.title.includes(filterForm.keyword) ||
      item.summary.includes(filterForm.keyword) ||
      item.owner.includes(filterForm.keyword)
    const hitType = filterForm.type === '全部' || item.type === filterForm.type
    const hitStatus = filterForm.status === '全部' || item.status === filterForm.status
    const hitTopic = filterForm.topic === '全部' || item.topic === filterForm.topic
    return hitKeyword && hitType && hitStatus && hitTopic
  }),
)

const selectedBriefing = computed(
  () => filteredList.value.find((item) => item.id === selectedBriefingId.value) ?? filteredList.value[0] ?? null,
)

const summaryMetrics = computed(() => [
  {
    id: 'all',
    label: '简报总量',
    value: briefingList.value.length,
    hint: '涵盖每日态势、专题、区域和事件专报',
    action: () => {
      filterForm.type = '全部'
      filterForm.status = '全部'
      filterForm.topic = '全部'
    },
  },
  {
    id: 'ready',
    label: '今日已生成',
    value: briefingList.value.filter((item) => item.status === '已生成').length,
    hint: '值班与专题研判可直接查看的简报数量',
    action: () => {
      filterForm.status = '已生成'
    },
  },
  {
    id: 'review',
    label: '待审核',
    value: briefingList.value.filter((item) => item.status === '待审核').length,
    hint: '建议优先审核事件专报和专题简报',
    action: () => {
      filterForm.status = '待审核'
    },
  },
  {
    id: 'topic',
    label: '专题简报',
    value: briefingList.value.filter((item) => item.type === '专题简报').length,
    hint: '经济和创新专题已形成固定模板输出能力',
    action: () => {
      filterForm.type = '专题简报'
    },
  },
])

/**
 * 简报中心支持从专题工作台和 GIS 首页通过 query 带入筛选条件与默认预览对象，
 * 保证“从发现到输出”的跳转链路落地后不会停在静态列表页。
 */
const applyRouteQuery = () => {
  const keyword = String(route.query.keyword ?? '')
  const topic = String(route.query.topic ?? '')
  const briefingId = String(route.query.briefingId ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (topic && topicOptions.value.includes(topic)) {
    filterForm.topic = topic
  }

  if (briefingId && briefingList.value.some((item) => item.id === briefingId)) {
    selectedBriefingId.value = briefingId
    return
  }

  if (autoOpen && filteredList.value.length) {
    selectedBriefingId.value = filteredList.value[0].id
  }
}

watch(() => route.query, applyRouteQuery)
watch(filteredList, (list) => {
  if (!list.length) {
    selectedBriefingId.value = ''
    return
  }

  if (!selectedBriefingId.value || !list.some((item) => item.id === selectedBriefingId.value)) {
    selectedBriefingId.value = list[0].id
  }
})

const openCreateDialog = (template?: BriefingTemplateItem) => {
  createForm.title = template ? `${template.title}-${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}` : ''
  createForm.type = template?.type ?? '专题简报'
  createForm.topic = template?.suggestedTopic ?? '经济专题'
  createForm.region = template?.type === '区域简报' ? '通州区' : '北京市'
  createForm.keyword = ''
  dialogVisible.value = true
}

const generateBriefing = async () => {
  if (!createForm.title.trim()) {
    ElMessage.warning('请输入简报标题')
    return
  }

  generating.value = true
  await new Promise((resolve) => setTimeout(resolve, 650))

  const newBriefing: BriefingListItem = {
    id: `BF-${Math.floor(Math.random() * 900000) + 100000}`,
    title: createForm.title.trim(),
    type: createForm.type,
    topic: createForm.topic,
    region: createForm.region,
    generatedAt: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
    owner: '综合研判组',
    status: '生成中',
    summary: `围绕 ${createForm.topic} 自动生成的 mock 简报，已汇总检索、预警与事件主线。`,
    sources: [createForm.keyword || `${createForm.topic} 主题检索结果`, '智能问答关联结论', 'GIS 热点区域态势'],
    keyPoints: ['已基于当前模板生成摘要', '后续可继续导出 Word / PDF', '支持回溯相关检索与报告来源'],
  }

  briefingList.value.unshift(newBriefing)
  selectedBriefingId.value = newBriefing.id
  generating.value = false
  dialogVisible.value = false
  ElMessage.success('简报已创建，正在生成（mock）')

  setTimeout(() => {
    newBriefing.status = '已生成'
  }, 1200)
}

const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.type = '全部'
  filterForm.status = '全部'
  filterForm.topic = '全部'
}

const previewBriefing = (id: string) => {
  selectedBriefingId.value = id
}

const openRelatedReport = () => {
  if (!selectedBriefing.value?.reportId) {
    ElMessage.info('当前简报暂无关联正式报告')
    return
  }
  router.push(`/report-center/detail/${selectedBriefing.value.reportId}`)
}

const exportBriefing = async (format: 'word' | 'pdf') => {
  if (!selectedBriefing.value) {
    ElMessage.info('请先选择简报')
    return
  }

  exportingId.value = format
  await new Promise((resolve) => setTimeout(resolve, 420))
  exportingId.value = ''
  ElMessage.success(`已导出 ${format.toUpperCase()}：${selectedBriefing.value.title}（mock）`)
}

const openQuestion = (topic: string) => {
  router.push({
    path: '/smart-qa',
    query: {
      topic: topic.includes('创新') ? 'innovation' : topic.includes('经济') ? 'economy' : 'all',
      question: `${topic} 当前最值得关注的三个信号是什么？`,
      autoAsk: 'true',
      from: 'briefing-center',
    },
  })
}

onMounted(() => {
  void loadBriefingData()
})
</script>

<template>
  <section class="briefing-center stage3-page">
    <WorkbenchHero
      eyebrow="Briefing Center"
      title="简报中心"
      description="统一管理每日态势简报、专题简报、区域简报和事件专报，形成正式输出链路。"
    >
      <template #meta>
        <el-button type="primary" @click="openCreateDialog()">新建简报</el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid v-if="!loading && !loadError" :items="summaryMetrics" />

    <ViewStatePanel
      v-if="loading"
      state="loading"
      title="正在加载简报中心数据"
      description="正在同步简报列表、模板和预览内容。"
    />
    <ViewStatePanel
      v-else-if="loadError"
      state="error"
      :description="loadError"
      @retry="loadBriefingData"
    />
    <template v-else>
      <div class="briefing-center__layout">
        <el-card shadow="never" class="briefing-panel">
          <template #header>
            <div class="briefing-panel__header">
              <div>
                <h3>简报列表</h3>
                <p>支持按类型、状态、专题快速筛选，并直接进入预览和导出。</p>
              </div>
              <el-button link type="primary" @click="resetFilter">重置筛选</el-button>
            </div>
          </template>

          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item>
              <el-input v-model="filterForm.keyword" placeholder="搜索标题 / 摘要 / 创建人" clearable />
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.type" style="width: 160px">
                <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.status" style="width: 130px">
                <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.topic" style="width: 150px">
                <el-option v-for="item in topicOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-form>

          <el-table :data="filteredList" stripe @row-click="previewBriefing($event.id)">
            <el-table-column label="简报标题" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click.stop="previewBriefing(row.id)">{{ row.title }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="topic" label="专题" width="120" />
            <el-table-column prop="generatedAt" label="生成时间" width="170" />
            <el-table-column prop="owner" label="创建人" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '已生成' ? 'success' : row.status === '待审核' ? 'warning' : 'info'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无简报数据" :image-size="72" />
            </template>
          </el-table>
        </el-card>

        <div class="briefing-side">
          <el-card shadow="never" class="briefing-panel">
            <template #header>
              <PageSectionHeader title="简报预览" description="选中后可直接查看关键摘要、来源和导出操作。" compact />
            </template>
            <ViewStatePanel
              v-if="!selectedBriefing"
              state="empty"
              compact
              title="暂无可预览简报"
              description="请调整筛选条件或新建一份简报。"
            />
            <template v-else>
              <div class="briefing-preview">
                <strong>{{ selectedBriefing.title }}</strong>
                <div class="briefing-preview__meta">
                  <span>{{ selectedBriefing.type }}</span>
                  <span>{{ selectedBriefing.topic }}</span>
                  <span>{{ selectedBriefing.region }}</span>
                  <span>{{ selectedBriefing.generatedAt }}</span>
                </div>
                <p>{{ selectedBriefing.summary }}</p>
              </div>

              <div class="briefing-block">
                <span class="briefing-block__label">关键结论</span>
                <ul>
                  <li v-for="point in selectedBriefing.keyPoints" :key="point">{{ point }}</li>
                </ul>
              </div>

              <div class="briefing-block">
                <span class="briefing-block__label">依据来源</span>
                <el-button v-for="source in selectedBriefing.sources" :key="source" link class="cell-link">
                  {{ source }}
                </el-button>
              </div>

              <div class="briefing-actions">
                <el-button type="primary" @click="openRelatedReport">查看关联报告</el-button>
                <el-button :loading="exportingId === 'word'" @click="exportBriefing('word')">导出 Word</el-button>
                <el-button :loading="exportingId === 'pdf'" @click="exportBriefing('pdf')">导出 PDF</el-button>
                <el-button @click="openQuestion(selectedBriefing.topic)">进入专题问答</el-button>
              </div>
            </template>
          </el-card>

          <el-card shadow="never" class="briefing-panel">
            <template #header>
              <PageSectionHeader title="四类简报模板" description="优先沉淀正式输出模板，避免重复拼装。" compact />
            </template>
            <div class="template-grid">
              <button
                v-for="item in templateList"
                :key="item.id"
                type="button"
                class="template-item"
                @click="openCreateDialog(item)"
              >
                <strong>{{ item.title }}</strong>
                <span>{{ item.scene }}</span>
                <small>{{ item.description }}</small>
              </button>
            </div>
          </el-card>
        </div>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" title="新建简报" width="620px">
      <el-form :model="createForm" label-width="88px">
        <el-form-item label="简报标题">
          <el-input v-model="createForm.title" placeholder="请输入简报标题" />
        </el-form-item>
        <el-form-item label="简报类型">
          <el-select v-model="createForm.type">
            <el-option v-for="item in typeOptions.filter((item) => item !== '全部')" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="专题/区域">
          <el-input v-model="createForm.topic" placeholder="如：经济专题 / 创新专题 / 通州区" />
        </el-form-item>
        <el-form-item label="覆盖区域">
          <el-input v-model="createForm.region" placeholder="请输入区域范围" />
        </el-form-item>
        <el-form-item label="检索关键词">
          <el-input v-model="createForm.keyword" placeholder="可选，用于记录本次简报线索来源" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="generateBriefing">生成简报</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.briefing-center {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.briefing-center__layout {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(360px, 1fr);
  gap: var(--platform-section-gap);
}

.briefing-side {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.briefing-panel {
  border: 1px solid var(--platform-border-subtle);
}

.briefing-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.briefing-panel__header h3 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.briefing-panel__header p {
  margin: 8px 0 0;
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.filter-form {
  margin-bottom: 10px;
}

.briefing-preview strong,
.template-item strong {
  color: var(--platform-text-primary);
}

.briefing-preview__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin: 10px 0;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.briefing-preview p,
.template-item small,
.template-item span,
.briefing-block ul {
  color: var(--platform-text-secondary);
}

.briefing-block + .briefing-block {
  margin-top: 14px;
}

.briefing-block__label {
  display: block;
  margin-bottom: 8px;
  color: var(--platform-text-primary);
  font-size: 13px;
  font-weight: 600;
}

.briefing-block ul {
  margin: 0;
  padding-left: 18px;
  line-height: 1.8;
}

.briefing-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.template-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: var(--platform-card-bg-muted);
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.template-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  box-shadow: 0 18px 38px rgba(2, 10, 20, 0.26);
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

@media (max-width: 1440px) {
  .briefing-center__layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .template-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import { warningRecordMock } from '../../mock/analysisWarning'
import type { WarningRecordItem } from '../../types/analysisWarning'

type LevelFilter = WarningRecordItem['level'] | '全部'
type StatusFilter = WarningRecordItem['status'] | '全部'

const recordList = ref<WarningRecordItem[]>(warningRecordMock.map((item) => ({ ...item })))
const detailVisible = ref(false)
const selectedRecord = ref<WarningRecordItem | null>(null)
const handlingIds = ref<string[]>([])
const route = useRoute()
const router = useRouter()

const filterForm = reactive({
  keyword: '',
  level: '全部' as LevelFilter,
  status: '全部' as StatusFilter,
  date: '',
})

const levelOptions: LevelFilter[] = ['全部', '高', '中', '低']
const statusOptions: StatusFilter[] = ['全部', '待处理', '处理中', '已处理']
const formatCount = (value: number) => new Intl.NumberFormat('zh-CN').format(value)

// 预警记录页既可能收到完整日期，也可能只收到月-日形式的钻取参数，
// 这里统一兼容首页图表和其他页面传入的两种时间格式。
const matchDate = (value: string, query: string) => {
  if (!query) {
    return true
  }

  if (value.includes(query)) {
    return true
  }

  if (/^\d{2}-\d{2}$/.test(query)) {
    const currentYear = new Date().getFullYear()
    return value.includes(`${currentYear}-${query}`)
  }

  return false
}

/**
 * 统一回到记录中心默认筛选态。
 * 这样从首页、图表、地图或规则页钻进来后，页面可以明确接管新的 query，而不是叠加旧条件。
 */
const resetFilterState = () => {
  filterForm.keyword = ''
  filterForm.level = '全部'
  filterForm.status = '全部'
  filterForm.date = ''
}

const filteredRecords = computed(() =>
  recordList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword ||
      item.title.includes(filterForm.keyword) ||
      item.sourceObject.includes(filterForm.keyword) ||
      item.triggerRule.includes(filterForm.keyword)
    const hitLevel = filterForm.level === '全部' || item.level === filterForm.level
    const hitStatus = filterForm.status === '全部' || item.status === filterForm.status
    const hitDate = matchDate(item.time, filterForm.date)
    return hitKeyword && hitLevel && hitStatus && hitDate
  }),
)

const levelTagMap: Record<string, 'danger' | 'warning' | 'info'> = {
  高: 'danger',
  中: 'warning',
  低: 'info',
}

const statusTagMap: Record<string, 'info' | 'warning' | 'success'> = {
  待处理: 'info',
  处理中: 'warning',
  已处理: 'success',
}

const focusedRecord = computed(() => selectedRecord.value ?? filteredRecords.value[0] ?? null)

const summaryCards = computed(() => {
  const total = filteredRecords.value.length
  const pending = filteredRecords.value.filter((item) => item.status === '待处理').length
  const processing = filteredRecords.value.filter((item) => item.status === '处理中').length
  const highRisk = filteredRecords.value.filter((item) => item.level === '高').length

  return [
    {
      id: 'all',
      label: '记录总量',
      value: formatCount(total),
      hint: '当前筛选范围内的预警记录',
      action: () => resetFilterState(),
    },
    {
      id: 'pending',
      label: '待处理',
      value: formatCount(pending),
      hint: '优先安排分派与研判',
      action: () => {
        filterForm.status = '待处理'
      },
    },
    {
      id: 'processing',
      label: '处理中',
      value: formatCount(processing),
      hint: '适合跟进当前处置流程',
      action: () => {
        filterForm.status = '处理中'
      },
    },
    {
      id: 'high',
      label: '高风险记录',
      value: formatCount(highRisk),
      hint: '需要优先联动规则与对象信息',
      action: () => {
        filterForm.level = '高'
      },
    },
  ]
})

const statusInsights = computed(() =>
  statusOptions
    .filter((item) => item !== '全部')
    .map((status) => ({
      status,
      count: filteredRecords.value.filter((item) => item.status === status).length,
    })),
)

const pendingQueue = computed(() =>
  filteredRecords.value.filter((item) => item.status !== '已处理').slice(0, 4),
)

const resetFilter = () => {
  resetFilterState()
  ElMessage.success('筛选条件已重置')
}

const openDetail = (item: WarningRecordItem) => {
  selectedRecord.value = item
  detailVisible.value = true
}

const toSource = (sourceObject: string) => {
  router.push({ path: '/smart-search', query: { keyword: sourceObject, from: 'warning-record' } })
}

const toRule = (ruleName: string) => {
  router.push({
    path: '/analysis-warning/rule-management',
    query: { keyword: ruleName, autoOpen: 'first', from: 'warning-record' },
  })
}

const toContent = (contentTitle: string) => {
  router.push({
    path: '/data-governance/standardized-list',
    query: { keyword: contentTitle, autoOpen: 'first', from: 'warning-record' },
  })
}

const isHandling = (id: string) => handlingIds.value.includes(id)

// 当前阶段没有真实后端，这里用前端状态模拟“标记已处理”闭环，
// 同时把处理动作补到时间线中，保证详情页反馈完整。
const markHandled = async (item: WarningRecordItem) => {
  if (item.status === '已处理') {
    ElMessage.info('该记录已处理')
    return
  }

  handlingIds.value.push(item.id)
  await new Promise((resolve) => setTimeout(resolve, 500))

  item.status = '已处理'
  item.timeline.push({
    time: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
    content: '已标记为处理完成（mock）',
  })

  handlingIds.value = handlingIds.value.filter((id) => id !== item.id)
  ElMessage.success('已标记为处理完成')
}

// 统一解析来自首页、地图和概览页的 query 参数，
// 让预警记录页支持按标题、等级、状态、日期和指定记录进行钻取定位。
const applyRouteQuery = () => {
  resetFilterState()
  detailVisible.value = false
  selectedRecord.value = null

  const keyword = String(route.query.keyword ?? '')
  const level = String(route.query.level ?? '全部')
  const status = String(route.query.status ?? '全部')
  const date = String(route.query.date ?? '')
  const recordId = String(route.query.recordId ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (levelOptions.includes(level as LevelFilter)) {
    filterForm.level = level as LevelFilter
  }

  if (statusOptions.includes(status as StatusFilter)) {
    filterForm.status = status as StatusFilter
  }

  if (date) {
    filterForm.date = date
  }

  const matched = recordList.value.find((item) => item.id === recordId)
  if (matched) {
    openDetail(matched)
    return
  }

  if (autoOpen && filteredRecords.value.length) {
    openDetail(filteredRecords.value[0])
  }
}

watch(() => route.query, applyRouteQuery, { immediate: true })

watch(filteredRecords, (list) => {
  if (!selectedRecord.value) {
    return
  }

  if (!list.some((item) => item.id === selectedRecord.value?.id)) {
    selectedRecord.value = list[0] ?? null
  }
})
</script>

<template>
  <section class="warning-records stage3-page">
    <WorkbenchHero
      eyebrow="Record Center"
      title="预警处置追踪工作台"
      description="把告警列表、当前焦点、处理状态和关联对象收敛到同一个记录中心，方便从发现一路追到处置闭环。"
    >
      <template #meta>
        <el-tag effect="plain">闭环处置可追踪</el-tag>
        <el-tag effect="plain" type="warning">支持待处理聚焦</el-tag>
        <el-tag effect="plain" type="success">状态反馈即时更新</el-tag>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid :items="summaryCards" />

    <div class="warning-records__main-grid">
      <div class="warning-records__primary">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="warning-records__card-header">
              <div class="platform-page-heading">
                <h3>记录列表</h3>
                <p>支持按标题、等级、状态和日期缩小范围，并继续钻取到规则、来源对象和内容详情。</p>
              </div>
              <el-tag effect="plain" type="info">当前 {{ filteredRecords.length }} 条</el-tag>
            </div>
          </template>

          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item>
              <el-input v-model="filterForm.keyword" placeholder="搜索预警标题/来源对象/触发规则" clearable />
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.level" style="width: 120px">
                <el-option label="全部等级" value="全部" />
                <el-option label="高" value="高" />
                <el-option label="中" value="中" />
                <el-option label="低" value="低" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.status" style="width: 120px">
                <el-option label="全部状态" value="全部" />
                <el-option label="待处理" value="待处理" />
                <el-option label="处理中" value="处理中" />
                <el-option label="已处理" value="已处理" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-date-picker
                v-model="filterForm.date"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="触发日期"
                clearable
                style="width: 150px"
              />
            </el-form-item>
            <el-form-item>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="filteredRecords" stripe>
            <el-table-column label="预警标题" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click="openDetail(row)">{{ row.title }}</el-button>
              </template>
            </el-table-column>
            <el-table-column label="告警等级" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="levelTagMap[row.level]">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="来源对象" min-width="140">
              <template #default="{ row }">
                <el-button link class="cell-link" @click="toSource(row.sourceObject)">{{ row.sourceObject }}</el-button>
              </template>
            </el-table-column>
            <el-table-column label="触发规则" min-width="170">
              <template #default="{ row }">
                <el-button link class="cell-link" @click="toRule(row.triggerRule)">{{ row.triggerRule }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="时间" width="160" />
            <el-table-column label="处理状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="statusTagMap[row.status]">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
                <el-button link :disabled="row.status === '已处理'" :loading="isHandling(row.id)" @click="markHandled(row)">
                  标记已处理
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无预警记录" :image-size="72" />
            </template>
          </el-table>
        </el-card>
      </div>

      <div class="warning-records__aside">
        <el-card shadow="never" class="detail-card warning-records__focus-card">
          <PageSectionHeader
            title="当前处置焦点"
            description="右侧聚焦当前记录的关键信息、处置建议和联动入口。"
            eyebrow="Focus"
            compact
          />

          <template v-if="focusedRecord">
            <div class="warning-records__focus-head">
              <div>
                <h4>{{ focusedRecord.title }}</h4>
                <p>{{ focusedRecord.description }}</p>
              </div>
              <el-tag size="small" :type="levelTagMap[focusedRecord.level]">{{ focusedRecord.level }}级</el-tag>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="来源对象">{{ focusedRecord.sourceObject }}</el-descriptions-item>
              <el-descriptions-item label="触发规则">{{ focusedRecord.triggerRule }}</el-descriptions-item>
              <el-descriptions-item label="处理状态">{{ focusedRecord.status }}</el-descriptions-item>
              <el-descriptions-item label="触发时间">{{ focusedRecord.time }}</el-descriptions-item>
            </el-descriptions>

            <div class="warning-records__action-row">
              <el-button type="primary" @click="openDetail(focusedRecord)">查看详情</el-button>
              <el-button @click="toRule(focusedRecord.triggerRule)">查看规则</el-button>
              <el-button @click="toSource(focusedRecord.sourceObject)">查看来源对象</el-button>
            </div>

            <div class="warning-records__suggestion-box">
              <span class="warning-records__subsection-title">建议处置</span>
              <p>{{ focusedRecord.suggestion }}</p>
            </div>
          </template>

          <el-empty v-else description="暂无可展示的预警焦点" :image-size="72" />
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="处理状态观察"
            description="帮助值守人员快速切换当前优先级。"
            eyebrow="Status"
            compact
          />
          <div class="warning-records__list-block">
            <button
              v-for="item in statusInsights"
              :key="item.status"
              type="button"
              class="warning-records__list-item"
              @click="filterForm.status = item.status"
            >
              <span>{{ item.status }}</span>
              <strong>{{ formatCount(item.count) }}</strong>
            </button>
          </div>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="待处理焦点"
            description="方便在不离开列表的前提下直接切换当前处理对象。"
            eyebrow="Queue"
            compact
          />
          <div class="warning-records__list-block">
            <button
              v-for="item in pendingQueue"
              :key="item.id"
              type="button"
              class="warning-records__list-item warning-records__list-item--column"
              @click="openDetail(item)"
            >
              <strong>{{ item.title }}</strong>
              <span>{{ item.time }} · {{ item.status }}</span>
            </button>
          </div>
        </el-card>
      </div>
    </div>

    <el-drawer v-model="detailVisible" title="预警记录详情" size="760px" destroy-on-close append-to-body>
      <template v-if="selectedRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预警标题" :span="2">{{ selectedRecord.title }}</el-descriptions-item>
          <el-descriptions-item label="告警等级">{{ selectedRecord.level }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">{{ selectedRecord.status }}</el-descriptions-item>
          <el-descriptions-item label="来源对象">{{ selectedRecord.sourceObject }}</el-descriptions-item>
          <el-descriptions-item label="触发规则">{{ selectedRecord.triggerRule }}</el-descriptions-item>
          <el-descriptions-item label="触发时间" :span="2">{{ selectedRecord.time }}</el-descriptions-item>
        </el-descriptions>

        <el-card shadow="never" class="detail-card">
          <template #header>预警描述</template>
          <p>{{ selectedRecord.description }}</p>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>触发原因</template>
          <p>{{ selectedRecord.triggerReason }}</p>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>关联内容</template>
          <el-empty v-if="!selectedRecord.relatedContents.length" description="暂无关联内容" :image-size="64" />
          <el-timeline v-else>
            <el-timeline-item v-for="item in selectedRecord.relatedContents" :key="item">
              <el-button link class="cell-link" @click="toContent(item)">{{ item }}</el-button>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>建议处置意见</template>
          <p>{{ selectedRecord.suggestion }}</p>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>处置时间线</template>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in selectedRecord.timeline"
              :key="`${item.time}-${index}`"
              :timestamp="item.time"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
.warning-records {
  min-width: 0;
}

.warning-records__metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--platform-card-gap);
}

.warning-records__main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(340px, 0.95fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.warning-records__aside {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.warning-records__card-header,
.warning-records__focus-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
}

.warning-records__focus-head {
  margin-top: 18px;
  margin-bottom: 18px;
}

.warning-records__focus-head h4 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.warning-records__focus-head p,
.warning-records__suggestion-box p,
.detail-card p {
  margin: 10px 0 0;
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.warning-records__action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.warning-records__suggestion-box {
  margin-top: 18px;
}

.warning-records__subsection-title {
  display: inline-flex;
  color: var(--platform-text-secondary);
  font-size: 12px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.warning-records__list-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.warning-records__list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  width: 100%;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: rgba(11, 21, 34, 0.82);
  padding: 14px 16px;
  color: var(--platform-text-primary);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.warning-records__list-item--column {
  align-items: flex-start;
  flex-direction: column;
}

.warning-records__list-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  background: rgba(14, 26, 42, 0.94);
}

.warning-records__list-item span {
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.warning-records__list-item strong {
  color: var(--platform-text-primary);
  font-size: 14px;
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

.detail-card {
  border: 1px solid var(--platform-border-subtle);
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

.warning-records :deep(.el-drawer__body) .detail-card + .detail-card {
  margin-top: 12px;
}

@media (max-width: 1800px) {
  .warning-records__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .warning-records__main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .warning-records__metric-grid {
    grid-template-columns: 1fr;
  }

  .warning-records__card-header,
  .warning-records__focus-head {
    flex-direction: column;
  }
}
</style>
